package ch.ethz.matsim.av.private_av;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;

public class PrivateVehicle extends AVVehicle {
	final private Link homeLink;
	final private PrivateSchedule privateSchedule;
	
	public PrivateVehicle(Id<Vehicle> id, Link startLink, double capacity, double t0, double t1, AVOperator operator, Link homeLink, PrivateSchedule schedule) {
		super(id, startLink, capacity, t0, t1, operator);
		this.homeLink = homeLink;
		this.privateSchedule = schedule;
	}
	
	public Link getHomeLink() {
		return homeLink;
	}
	
	public PrivateSchedule getPrivateSchedule() {
		return privateSchedule;
	}
}
