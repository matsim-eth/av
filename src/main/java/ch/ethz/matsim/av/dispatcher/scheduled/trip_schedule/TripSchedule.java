package ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

public class TripSchedule {
	final private List<Trip> trips = new LinkedList<>();
	final private String name;
	
	private Link homeLink;
	private Link startLink;
	
	public TripSchedule(String name) {
		this.name = name;
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
	
	public void addTrip(Trip trip) {
		trips.add(trip);
	}

	public List<Trip> getTrips() {
		return trips;
	}
	
	public String getName() {
		return name;
	}
}
