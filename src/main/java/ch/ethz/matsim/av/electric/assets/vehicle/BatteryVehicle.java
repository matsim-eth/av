package ch.ethz.matsim.av.electric.assets.vehicle;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;

import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.electric.assets.battery.Battery;
import ch.ethz.matsim.av.electric.consumption.ConsumptionCalculator;

public class BatteryVehicle extends AVVehicle {
	final private Battery battery;
	final private ConsumptionCalculator consumptionCalculator;
	
	public BatteryVehicle(Id<Vehicle> id, Link startLink, double capacity, double t0, double t1, Battery battery, ConsumptionCalculator consumptionCalculator) {
		super(id, startLink, capacity, t0, t1);
		
		this.battery = battery;
		this.consumptionCalculator = consumptionCalculator;
	}
	
	public Battery getBattery() {
		return battery;
	}
	
	public ConsumptionCalculator getConsumptionCalculator() {
		return consumptionCalculator;
	}
}
