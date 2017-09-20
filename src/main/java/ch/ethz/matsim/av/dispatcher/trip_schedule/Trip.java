package ch.ethz.matsim.av.dispatcher.trip_schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.households.Household;

import ch.ethz.matsim.av.routing.AVRoute;

abstract public class Trip {
	final private double pickupTime;
	final private Link pickupLink;
	final private Link dropoffLink;
	final private AVRoute route;

	public Trip(double pickupTime, Link pickupLink, Link dropoffLink, AVRoute route) {
		this.pickupTime = pickupTime;
		this.pickupLink = pickupLink;
		this.dropoffLink = dropoffLink;
		this.route = route;
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
}
