package ch.ethz.matsim.av.electric.assets.station.events;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public class RechargeEndEvent extends AbstractStationInteractionEvent {
	public RechargeEndEvent(double time, Id<Station> stationId, Id<Vehicle> vehicleId,
			double chargeState) {
		super("RechargeEnd", time, stationId, vehicleId, chargeState);
	}
}
