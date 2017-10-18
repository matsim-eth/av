package ch.ethz.matsim.av.electric.assets.battery;

public interface Battery {
	BatterySpecification getSpecification();
	
	double getState();
	void setState(double state);
}
