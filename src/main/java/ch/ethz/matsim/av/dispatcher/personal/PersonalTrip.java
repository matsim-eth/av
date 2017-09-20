package ch.ethz.matsim.av.dispatcher.personal;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.dispatcher.trip_schedule.Trip;
import ch.ethz.matsim.av.routing.AVRoute;

public class PersonalTrip extends Trip {
	public PersonalTrip(double pickupTime, Link pickupLink, Link dropoffLink, AVRoute route) {
		super(pickupTime, pickupLink, dropoffLink, route);
	}
}
