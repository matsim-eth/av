package ch.ethz.matsim.av.electric.calculators;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StaticChargeCalculator implements ChargeCalculator {
    final private StaticChargeCalculatorConfig config;
    final private double S_PER_H = 3600.0;

    @Inject
    public StaticChargeCalculator(StaticChargeCalculatorConfig config) {
        this.config = config;
    }

    @Override
    public double calculateConsumption(double from, double until, double distance) {
        return (distance / 1e-3) * config.getDischargeRateByDistance();
    }

    @Override
    public double calculateConsumption(double from, double until) {
        return (until - from) / S_PER_H * config.getDischargeRateByTime();
    }

    @Override
    public double getInitialCharge(double now) {
        return config.getMaximumCharge();
    }

    @Override
    public double getMaximumCharge(double now) {
        return config.getMaximumCharge();
    }

    @Override
    public boolean isCritical(double charge, double now) {
        return charge < config.getMinimumCharge();
    }

    @Override
    public double getRechargeTime(double now) {
        return S_PER_H * (config.getMaximumCharge() - config.getMinimumCharge()) / config.getRechargeRatePerTime();
    }
}
