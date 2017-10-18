package ch.ethz.matsim.av.electric.assets.station.events;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public class VehicleArrivalEvent extends AbstractStationInteractionEvent {
	public VehicleArrivalEvent(double time, Id<Station> stationId, Id<Vehicle> vehicleId,
			double chargeState) {
		super("VehicleArrival", time, stationId, vehicleId, chargeState);
	}
}
