package ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.av.routing.AVRoute;

public class Trip {
	final private double pickupTime;
	final private Link pickupLink;
	final private Link dropoffLink;
	final private AVRoute route;
	final private Person person;
	final private StopLocation stopLocation;
	
	public enum StopLocation {
		DROPOFF, HOME //, PICKUP
	}
	
	public Trip(double pickupTime, Link pickupLink, Link dropoffLink, AVRoute route, Person person, StopLocation stopLocation) {
		this.pickupTime = pickupTime;
		this.pickupLink = pickupLink;
		this.dropoffLink = dropoffLink;
		this.route = route;
		this.person = person;
		this.stopLocation = stopLocation;
	}

	public double getPickupTime() {
		return pickupTime;
	}

	public Link getPickupLink() {
		return pickupLink;
	}

	public Link getDropoffLink() {
		return dropoffLink;
	}

	public AVRoute getRoute() {
		return route;
	}

	public Person getPerson() {
		return person;
	}
	
	public StopLocation getStopLocation() {
		return stopLocation;
	}
}
