package ch.ethz.matsim.av.private_av;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.pt.PtConstants;

import ch.ethz.matsim.av.routing.AVRoute;

public class PrivateScheduleRepository {
	final private List<PrivateSchedule> schedules = new LinkedList<>();

	final private Population population;
	final private Network network;

	public PrivateScheduleRepository(Population population, Network network) {
		this.population = population;
		this.network = network;
	}

	public Collection<PrivateSchedule> getSchedules() {
		return Collections.unmodifiableCollection(schedules);
	}

	public void update() {
		schedules.clear();
		
		for (Person person : population.getPersons().values()) {
			PrivateSchedule schedule = new PrivateSchedule();
			boolean isUsingPrivateAV = false;

			for (TripStructureUtils.Trip trip : TripStructureUtils.getTrips(person.getSelectedPlan(),
					new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE))) {
				Leg leg = trip.getLegsOnly().iterator().next();

				if (leg.getMode().equals("av")) {
					AVRoute route = (AVRoute) leg.getRoute();

					if (route != null && route.getOperatorId().toString().equals("private")) {
						isUsingPrivateAV = true;

						schedule.addTrip(trip.getOriginActivity().getEndTime(),
								network.getLinks().get(trip.getOriginActivity().getLinkId()),
								network.getLinks().get(trip.getDestinationActivity().getLinkId()));

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
				
				schedule.setPerson(person);
				schedules.add(schedule);
				System.err.println("ADDING SCHEDULE FOR " + person.getId());
			}
		}
		
		System.exit(1);
	}
}
