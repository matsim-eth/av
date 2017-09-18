package ch.ethz.matsim.av.generator;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.vehicles.VehicleType;

import ch.ethz.matsim.av.data.AVVehicle;

public interface AVVehicleFactory {
	AVVehicle createVehicle(Id<Vehicle> id, Link startLink, double capacity, double startTime, double endTime);
	VehicleType getVehicleType();
}
