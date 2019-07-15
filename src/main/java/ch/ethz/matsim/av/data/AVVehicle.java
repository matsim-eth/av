package ch.ethz.matsim.av.data;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleImpl;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;

import ch.ethz.matsim.av.dispatcher.AVDispatcher;

public class AVVehicle extends DvrpVehicleImpl {
	private AVOperator operator;
	private AVDispatcher dispatcher;

	public AVVehicle(Id<DvrpVehicle> id, Link startLink, int capacity, double t0, double t1, AVOperator operator) {
		super(ImmutableDvrpVehicleSpecification.newBuilder().id(id).capacity(capacity).startLinkId(startLink.getId())
				.serviceBeginTime(t0).serviceEndTime(t1).build(), startLink);
		this.operator = operator;
	}

	public AVVehicle(Id<DvrpVehicle> id, Link startLink, int capacity, double t0, double t1) {
		this(id, startLink, capacity, t0, t1, null);
	}

	public AVOperator getOperator() {
		return operator;
	}

	public AVDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setOpeartor(AVOperator operator) {
		this.operator = operator;
	}

	public void setDispatcher(AVDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
}
