package ch.ethz.matsim.av.routing;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.Facility;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.replanning.AVOperatorChoiceStrategy;
import ch.ethz.matsim.av.routing.interaction.AVInteractionFinder;
import ch.ethz.matsim.av.waiting_time.WaitingTime;

public class AVRoutingModule implements RoutingModule {
	static final public String INTERACTION_ACTIVITY_TYPE = "av interaction";

	private final AVOperatorChoiceStrategy choiceStrategy;
	private final AVRouteFactory routeFactory;
	private final RoutingModule walkRoutingModule;
	private final PopulationFactory populationFactory;
	private final RoutingModule roadRoutingModule;

	private Map<Id<AVOperator>, AVInteractionFinder> interactionFinders;
	private Map<Id<AVOperator>, WaitingTime> waitingTimes;
	private final boolean useAccessEgress;
	private final Map<Id<AVOperator>, Boolean> predictRouteTravelTime;

	public AVRoutingModule(AVOperatorChoiceStrategy choiceStrategy, AVRouteFactory routeFactory,
			Map<Id<AVOperator>, AVInteractionFinder> interactionFinders, Map<Id<AVOperator>, WaitingTime> waitingTimes,
			PopulationFactory populationFactory, RoutingModule walkRoutingModule, boolean useAccessEgress,
			Map<Id<AVOperator>, Boolean> predictRouteTravelTime, RoutingModule roadRoutingModule) {
		this.choiceStrategy = choiceStrategy;
		this.routeFactory = routeFactory;
		this.interactionFinders = interactionFinders;
		this.waitingTimes = waitingTimes;
		this.walkRoutingModule = walkRoutingModule;
		this.populationFactory = populationFactory;
		this.useAccessEgress = useAccessEgress;
		this.predictRouteTravelTime = predictRouteTravelTime;
		this.roadRoutingModule = roadRoutingModule;
	}

	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person) {
		Id<AVOperator> operatorId = choiceStrategy.chooseRandomOperator();
		return calcRoute(fromFacility, toFacility, departureTime, person, operatorId);
	}

	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person, Id<AVOperator> operatorId) {
		AVInteractionFinder interactionFinder = interactionFinders.get(operatorId);
		WaitingTime waitingTime = waitingTimes.get(operatorId);

		Facility pickupFacility = interactionFinder.findPickupFacility(fromFacility, departureTime);
		Facility dropoffFacility = interactionFinder.findDropoffFacility(toFacility, departureTime);

		if (pickupFacility.getLinkId().equals(dropoffFacility.getLinkId())) {
			// Special case: PassengerEngine will complain that request has same start and
			// end link. In that case we just return walk.
			return walkRoutingModule.calcRoute(fromFacility, toFacility, departureTime, person);
		}

		double pickupEuclideanDistance = CoordUtils.calcEuclideanDistance(fromFacility.getCoord(),
				pickupFacility.getCoord());
		double dropoffEuclideanDistance = CoordUtils.calcEuclideanDistance(dropoffFacility.getCoord(),
				toFacility.getCoord());

		// First, we need to access (if configured)
		double accessDepartureTime = departureTime;
		double requestSendTime = departureTime;

		List<PlanElement> routeElements = new LinkedList<>();

		if (fromFacility != pickupFacility && useAccessEgress && pickupEuclideanDistance > 0.0) {
			List<? extends PlanElement> pickupElements = walkRoutingModule.calcRoute(fromFacility, pickupFacility,
					accessDepartureTime, person);
			routeElements.addAll(pickupElements);

			Activity pickupActivity = populationFactory.createActivityFromLinkId(INTERACTION_ACTIVITY_TYPE,
					pickupFacility.getLinkId());
			pickupActivity.setMaximumDuration(0.0);
			routeElements.add(pickupActivity);

			if (pickupElements.size() != 1) {
				throw new IllegalStateException();
			}

			// If we have a non-zero access leg, the request for the AV will be issued after
			// arrival
			requestSendTime += ((Leg) pickupElements.get(0)).getTravelTime();
		}

		// Now waiting for the vehicle

		// Given the request time, we can calculate the waiting time
		double vehicleWaitingTime = waitingTime.getWaitingTime(pickupFacility, requestSendTime);
		double vehicleDepartureTime = requestSendTime + vehicleWaitingTime;

		// Here we can optionally already route the AV leg to estimate travel time
		// TODO: Maybe we can hide this behind a AVTravelTimePredictor interface or
		// something like that
		boolean doPredictTravelTime = predictRouteTravelTime.get(operatorId);

		double vehicleDistance = Double.NaN;
		double vehicleTravelTime = Double.NaN;

		if (doPredictTravelTime) {
			List<? extends PlanElement> transitElements = roadRoutingModule.calcRoute(pickupFacility, dropoffFacility,
					vehicleDepartureTime, null);

			if (transitElements.size() != 1) {
				throw new IllegalStateException("Expected one element in downstream routing module");
			}

			Leg leg = (Leg) transitElements.get(0);
			vehicleDistance = leg.getRoute().getDistance();
			vehicleTravelTime = leg.getRoute().getTravelTime();
		}

		double totalTravelTime = vehicleTravelTime + vehicleWaitingTime;

		// Build Route and Leg
		AVRoute route = routeFactory.createRoute(pickupFacility.getLinkId(), dropoffFacility.getLinkId());
		route.setOperatorId(operatorId);
		route.setDistance(vehicleDistance);
		route.setTravelTime(totalTravelTime);
		route.setWaitingTime(vehicleWaitingTime);
		route.setInVehicleTime(vehicleTravelTime);

		Leg leg = populationFactory.createLeg(AVModule.AV_MODE);
		leg.setDepartureTime(requestSendTime);
		leg.setTravelTime(totalTravelTime);
		leg.setRoute(route);

		routeElements.add(leg);

		// Now we need to egress
		double egressDepartureTime = vehicleDepartureTime; // The last we know, if we don't route

		if (Double.isNaN(totalTravelTime)) {
			// If we have routed the leg, we know it better
			egressDepartureTime = totalTravelTime;
		}

		if (toFacility != dropoffFacility && useAccessEgress && dropoffEuclideanDistance > 0.0) {
			Activity dropoffActivity = populationFactory.createActivityFromLinkId(INTERACTION_ACTIVITY_TYPE,
					dropoffFacility.getLinkId());
			dropoffActivity.setMaximumDuration(0.0);
			routeElements.add(dropoffActivity);

			List<? extends PlanElement> dropoffElements = walkRoutingModule.calcRoute(dropoffFacility, toFacility,
					egressDepartureTime, person);
			routeElements.addAll(dropoffElements);
		}

		return routeElements;
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		return new StageActivityTypesImpl(INTERACTION_ACTIVITY_TYPE);
	}
}
