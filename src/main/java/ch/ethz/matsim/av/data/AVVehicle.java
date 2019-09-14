package ch.ethz.matsim.av.data;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.VehicleImpl;

import ch.ethz.matsim.av.dispatcher.AVDispatcher;

import org.matsim.vehicles.VehicleType;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;

public class AVVehicle extends VehicleImpl {
	private AVOperator operator = null;
	private AVDispatcher dispatcher;
	private VehicleType vehicleType;

	public AVVehicle(Id<Vehicle> id, Link startLink, VehicleType vehicleType, double t0, double t1, AVOperator operator) {
		super(id, startLink, vehicleType.getCapacity().getSeats(), t0, t1);
		this.operator = operator;
		this.vehicleType = vehicleType;
	}

	public AVVehicle(Id<Vehicle> id, Link startLink, VehicleType vehicleType, double t0, double t1) {
		this(id, startLink, vehicleType, t0, t1, null);
	}

	public AVOperator getOperator() {
		return operator;
	}

	public AVDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setOperator(AVOperator operator) {
		this.operator = operator;
	}

	public void setDispatcher(AVDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}
}
