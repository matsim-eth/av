package ch.ethz.matsim.av.electric.calculators;

public interface ChargeCalculator {
    double calculateConsumption(double from, double until, double distance);
    double calculateConsumption(double from, double until);

    double getInitialCharge(double now);
    double getMaximumCharge(double now);

    boolean isCritical(double charge, double now);
    double getRechargeTime(double now);
}
