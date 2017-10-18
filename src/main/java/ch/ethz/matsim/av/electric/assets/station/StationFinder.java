package ch.ethz.matsim.av.electric.assets.station;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public interface StationFinder {
	Station findStationForVehicle(BatteryVehicle vehicle, Link link);
}
