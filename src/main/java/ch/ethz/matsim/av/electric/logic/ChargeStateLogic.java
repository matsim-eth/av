package ch.ethz.matsim.av.electric.logic;

import org.matsim.contrib.dvrp.schedule.Task;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public interface ChargeStateLogic {
	void registerConsumptionForTask(BatteryVehicle vehicle, Task task);
	void simulate(double now);
}
