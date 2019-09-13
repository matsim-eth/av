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

public class AVRoutingModule implements RoutingModule {
	static final public String INTERACTION_ACTIVITY_TYPE = "av interaction";

	private final AVOperatorChoiceStrategy choiceStrategy;
	private final AVRouteFactory routeFactory;
	private final RoutingModule walkRoutingModule;
	private final PopulationFactory populationFactory;

	private Map<Id<AVOperator>, AVInteractionFinder> interactionFinders;
	private final boolean useAccessEgress;

	public AVRoutingModule(AVOperatorChoiceStrategy choiceStrategy, AVRouteFactory routeFactory,
			Map<Id<AVOperator>, AVInteractionFinder> interactionFinders, PopulationFactory populationFactory,
			RoutingModule walkRoutingModule, boolean useAccessEgress) {
		this.choiceStrategy = choiceStrategy;
		this.routeFactory = routeFactory;
		this.interactionFinders = interactionFinders;
		this.walkRoutingModule = walkRoutingModule;
		this.populationFactory = populationFactory;
		this.useAccessEgress = useAccessEgress;
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
		
		Facility pickupFacility = interactionFinder.findPickupFacility(fromFacility, departureTime);
		Facility dropoffFacility = interactionFinder.findDropoffFacility(toFacility, departureTime);

		if (pickupFacility.getLinkId().equals(dropoffFacility.getLinkId())) {
			// Special case: PassengerEngine will complain that request has same start and
			// end link. In that case we just return walk.
			return walkRoutingModule.calcRoute(fromFacility, toFacility, departureTime, person);
		}

		double pickupDistance = CoordUtils.calcEuclideanDistance(fromFacility.getCoord(), pickupFacility.getCoord());
		double dropoffDistance = CoordUtils.calcEuclideanDistance(dropoffFacility.getCoord(), toFacility.getCoord());

		double pickupTime = departureTime;

		List<PlanElement> routeElements = new LinkedList<>();

		if (fromFacility != pickupFacility && useAccessEgress && pickupDistance > 0.0) {
			List<? extends PlanElement> pickupElements = walkRoutingModule.calcRoute(fromFacility, pickupFacility,
					departureTime, person);
			routeElements.addAll(pickupElements);

			Activity pickupActivity = populationFactory.createActivityFromLinkId(INTERACTION_ACTIVITY_TYPE,
					pickupFacility.getLinkId());
			pickupActivity.setMaximumDuration(0.0);
			routeElements.add(pickupActivity);

			if (pickupElements.size() != 1) {
				throw new IllegalStateException();
			}

			pickupTime = ((Leg) pickupElements.get(0)).getTravelTime();
		}

		AVRoute route = routeFactory.createRoute(pickupFacility.getLinkId(), dropoffFacility.getLinkId());
		route.setOperatorId(operatorId);
		route.setDistance(Double.NaN);
		route.setTravelTime(Double.NaN);

		Leg leg = populationFactory.createLeg(AVModule.AV_MODE);
		leg.setDepartureTime(pickupTime);
		leg.setTravelTime(Double.NaN);
		leg.setRoute(route);

		routeElements.add(leg);

		if (toFacility != dropoffFacility && useAccessEgress && dropoffDistance > 0.0) {
			Activity dropoffActivity = populationFactory.createActivityFromLinkId(INTERACTION_ACTIVITY_TYPE,
					dropoffFacility.getLinkId());
			dropoffActivity.setMaximumDuration(0.0);
			routeElements.add(dropoffActivity);

			List<? extends PlanElement> dropoffElements = walkRoutingModule.calcRoute(dropoffFacility, toFacility,
					departureTime, person);
			routeElements.addAll(dropoffElements);
		}

		return routeElements;
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		return new StageActivityTypesImpl(INTERACTION_ACTIVITY_TYPE);
	}
}
