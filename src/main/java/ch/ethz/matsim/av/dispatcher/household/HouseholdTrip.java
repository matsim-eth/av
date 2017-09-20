package ch.ethz.matsim.av.dispatcher.household;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.av.dispatcher.trip_schedule.Trip;
import ch.ethz.matsim.av.routing.AVRoute;

public class HouseholdTrip extends Trip {
	final private Person person;
	
	public HouseholdTrip(double pickupTime, Link pickupLink, Link dropoffLink, AVRoute route, Person person) {
		super(pickupTime, pickupLink, dropoffLink, route);
		this.person = person;
	}
	
	public Person getPerson() {
		return person;
	}
}
