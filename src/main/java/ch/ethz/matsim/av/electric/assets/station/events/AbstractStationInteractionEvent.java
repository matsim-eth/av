package ch.ethz.matsim.av.electric.assets.station.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public abstract class AbstractStationInteractionEvent extends GenericEvent {
	final private Id<Station> stationId;
	final private Id<Vehicle> vehicleId;
	final private double chargeState;
	
	public AbstractStationInteractionEvent(String type, double time, Id<Station> stationId, Id<Vehicle> vehicleId, double chargeState) {
		super(type, time);
		
		this.stationId = stationId;
		this.vehicleId = vehicleId;
		this.chargeState = chargeState;
		
		getAttributes().put("station", stationId.toString());
		getAttributes().put("vehicle", vehicleId.toString());
		getAttributes().put("chargeState", String.valueOf(chargeState));
	}
	
	public Id<Station> getStationId() {
		return stationId;
	}
	
	public Id<Vehicle> getVehicleId() {
		return vehicleId;
	}
	
	public double getChargeState() {
		return chargeState;
	}
}
