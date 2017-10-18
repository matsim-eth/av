package ch.ethz.matsim.av.electric.assets.station.events;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.matsim.av.electric.assets.station.Station;

public class RechargeStartEvent extends AbstractStationInteractionEvent {

	public RechargeStartEvent(double time, Id<Station> stationId, Id<Vehicle> vehicleId,
			double chargeState) {
		super("RechargeStart", time, stationId, vehicleId, chargeState);
	}
	
}
