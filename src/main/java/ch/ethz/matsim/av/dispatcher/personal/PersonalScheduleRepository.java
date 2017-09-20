package ch.ethz.matsim.av.dispatcher.personal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.pt.PtConstants;

import ch.ethz.matsim.av.routing.AVRoute;

public class PersonalScheduleRepository {
	final private Population population;
	final private Network network;

	final private String dispatcherName;

	public PersonalScheduleRepository(Population population, Network network, String dispatcherName) {
		this.population = population;
		this.network = network;
		this.dispatcherName = dispatcherName;
	}

	public Collection<PersonalSchedule> getSchedules() {
		List<PersonalSchedule> schedules = new LinkedList<>();

		for (Person person : population.getPersons().values()) {
			PersonalSchedule schedule = new PersonalSchedule(person);
			boolean isUsingPrivateAV = false;

			for (TripStructureUtils.Trip trip : TripStructureUtils.getTrips(person.getSelectedPlan(),
					new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE))) {
				Leg leg = trip.getLegsOnly().iterator().next();

				if (leg.getMode().equals("av")) {
					AVRoute route = (AVRoute) leg.getRoute();

					if (route != null && route.getOperatorId().toString().equals(dispatcherName)) {
						isUsingPrivateAV = true;

						PersonalTrip scheduleTrip = new PersonalTrip(trip.getOriginActivity().getEndTime(),
								network.getLinks().get(trip.getOriginActivity().getLinkId()),
								network.getLinks().get(trip.getDestinationActivity().getLinkId()), route);

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
}
