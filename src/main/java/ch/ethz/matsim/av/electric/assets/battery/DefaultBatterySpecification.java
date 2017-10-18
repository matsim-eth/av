package ch.ethz.matsim.av.electric.assets.battery;

public class DefaultBatterySpecification implements BatterySpecification {
	final private double rechargeRate;
	final private double maximumEnergy;
	
	public DefaultBatterySpecification(double rechargeRate, double maximumEnergy) {
		this.rechargeRate = rechargeRate;
		this.maximumEnergy = maximumEnergy;
	}
	
	@Override
	public double getRechargeRate(double temperature) {
		return rechargeRate;
	}

	@Override
	public double getMaximumEnergy() {
		return maximumEnergy;
	}
}
