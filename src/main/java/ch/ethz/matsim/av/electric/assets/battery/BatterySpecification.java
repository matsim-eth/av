package ch.ethz.matsim.av.electric.assets.battery;

public interface BatterySpecification {
	double getRechargeRate(double temperature);
	double getMaximumEnergy();
}
