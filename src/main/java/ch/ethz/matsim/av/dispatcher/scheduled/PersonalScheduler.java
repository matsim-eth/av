package ch.ethz.matsim.av.dispatcher.scheduled;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.pt.PtConstants;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule.Trip;
import ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule.TripSchedule;
import ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule.TripScheduler;
import ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule.TripSchedulerFactory;
import ch.ethz.matsim.av.routing.AVRoute;

/**
 * This scheduler creates AV schedules for single persons.
 * 
 * This means one AV is created per person that intends to use an
 * AV with the respective operator.
 * 
 * The vehicle will be initialized at the first activity of the agent
 * and has a home location, which is the home location of the agent.
 */
public class PersonalScheduler implements TripScheduler {
	final private Population population;
	final private Network network;

	final private Id<AVOperator> operatorId;
	final private boolean returnHome;

	public PersonalScheduler(Population population, Network network, Id<AVOperator> operatorId, boolean returnHome) {
		this.population = population;
		this.network = network;
		this.operatorId = operatorId;
		this.returnHome = returnHome;
	}

	@Override
	public Collection<TripSchedule> getSchedules() {
		List<TripSchedule> schedules = new LinkedList<>();

		for (Person person : population.getPersons().values()) {
			TripSchedule schedule = new TripSchedule("p" + person.getId().toString());
			boolean isUsingPrivateAV = false;

			for (TripStructureUtils.Trip trip : TripStructureUtils.getTrips(person.getSelectedPlan(),
					new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE))) {
				Leg leg = trip.getLegsOnly().iterator().next();

				if (leg.getMode().equals("av")) {
					AVRoute route = (AVRoute) leg.getRoute();

					if (route != null && route.getOperatorId().equals(operatorId)) {
						isUsingPrivateAV = true;

						Trip scheduleTrip = new Trip(trip.getOriginActivity().getEndTime(),
								network.getLinks().get(trip.getOriginActivity().getLinkId()),
								network.getLinks().get(trip.getDestinationActivity().getLinkId()), route, person, returnHome);

						schedule.addTrip(scheduleTrip);

						if (schedule.getStartLink() == null) {
							schedule.setStartLink(network.getLinks().get(trip.getOriginActivity().getLinkId()));
						}
					}
				}

				if (trip.getOriginActivity().getType().equals("home") && schedule.getHomeLink() == null) {
					schedule.setHomeLink(network.getLinks().get(trip.getOriginActivity().getLinkId()));
				}
			}

			if (isUsingPrivateAV) {
				if (schedule.getHomeLink() == null) {
					schedule.setHomeLink(schedule.getStartLink());
				}

				schedules.add(schedule);
			}
		}

		return schedules;
	}
	
	@Singleton
	static public class Factory implements TripSchedulerFactory {
		@Inject Population population;
		@Inject Network network;
		
		@Override
		public TripScheduler createScheduler(Id<AVOperator> operatorId) {
			return new PersonalScheduler(population, network, operatorId, true);
		}
	}
}
