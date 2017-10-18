package ch.ethz.matsim.av.electric.logic;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public interface RechargingController {
	boolean shouldRecharge(BatteryVehicle vehicle);
	void simulate(double now);
}
