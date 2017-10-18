package ch.ethz.matsim.av.electric.calculators;

public interface ChargeCalculator {
    double calculateDistanceBasedConsumption(double from, double until, double distance);
    double calculateTimeBasedConsumption(double from, double until);

    double getInitialCharge(double now);
    double getMaximumCharge(double now);

    boolean isCritical(double charge, double now);
    double getRechargeTime(double now, double startCharge);
    
    double getRechargeRate(double now);
}
