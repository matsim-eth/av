package ch.ethz.matsim.av.dispatcher.trip_schedule;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

abstract public class TripSchedule<T extends Trip> {
	final private List<T> trips = new LinkedList<>();
	
	private Link homeLink;
	private Link startLink;

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
	
	public void addTrip(T trip) {
		trips.add(trip);
	}

	public List<T> getTrips() {
		return trips;
	}
}
