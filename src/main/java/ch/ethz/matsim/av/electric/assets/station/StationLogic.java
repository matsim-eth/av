package ch.ethz.matsim.av.electric.assets.station;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public interface StationLogic {
	boolean proceedToRecharge(BatteryVehicle vehicle);
	boolean finishRecharging(BatteryVehicle vehicle);
	
	void notifyRegistration(BatteryVehicle vehicle, double now);
	void notifyArrival(BatteryVehicle vehicle, double now);
	void notifyStartRecharge(BatteryVehicle vehicle, double now);
}
