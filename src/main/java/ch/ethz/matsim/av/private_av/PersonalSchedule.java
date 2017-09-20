package ch.ethz.matsim.av.private_av;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.av.routing.AVRoute;

public class PersonalSchedule {
	public class Trip {
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

	final private List<Trip> trips = new LinkedList<>();

	private Link homeLink;
	private Link startLink;

	private Person person;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Link getHomeLink() {
		return homeLink;
	}

	public Link getStartLink() {
		return startLink;
	}

	public void setHomeLink(Link homeLink) {
		this.homeLink = homeLink;
	}

	public void setStartLink(Link startLink) {
		this.startLink = startLink;
	}

	public void addTrip(double pickupTime, Link pickupLink, Link dropoffLink, AVRoute route) {
		trips.add(new Trip(pickupTime, pickupLink, dropoffLink, route));
	}

	public List<Trip> getTrips() {
		return trips;
	}
}
