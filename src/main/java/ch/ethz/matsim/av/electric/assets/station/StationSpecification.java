package ch.ethz.matsim.av.electric.assets.station;

public interface StationSpecification {
	double getRechargeRate(double temperature);
}
