package ch.ethz.matsim.av.electric.logic.action;

import org.matsim.contrib.dynagent.AbstractDynActivity;

import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public class WaitForRechargeActivity extends AbstractDynActivity {
	final private BatteryVehicle vehicle;
	final private Station station;
	
	private double endTime = Double.MAX_VALUE;
	
	public WaitForRechargeActivity(BatteryVehicle vehicle, Station station) {
		super("WaitForRecharge");
		
		this.vehicle = vehicle;
		this.station = station;
	}

	@Override
	public double getEndTime() {
		return endTime;
	}
	
	@Override
	public void doSimStep(double now) {
		if (station.proceedToRecharge(vehicle)) {
			endTime = now;
		}
	}
}
