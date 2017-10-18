package ch.ethz.matsim.av.electric.logic;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public class ThresholdRechargingController implements RechargingController {
	final private double threshold;
	
	public ThresholdRechargingController(double threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public boolean shouldRecharge(BatteryVehicle vehicle) {
		return vehicle.getBattery().getState() <= threshold;
	}

	@Override
	public void simulate(double now) {}
}
