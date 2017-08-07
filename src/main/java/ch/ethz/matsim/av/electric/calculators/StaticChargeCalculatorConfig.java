package ch.ethz.matsim.av.electric.calculators;

import org.matsim.core.config.ReflectiveConfigGroup;

public class StaticChargeCalculatorConfig extends ReflectiveConfigGroup {
    static final String STATIC_CHARGE_CALCULATOR = "static_charge_calculator";

    static final String DISCHARGE_RATE_BY_DISTANCE = "dischargeRateByDistance_kWh_per_km";
    static final String DISCHARGE_RATE_BY_TIME = "dischargeRateByTime_kW";
    static final String MAXIMUM_CHARGE = "maximumCharge_kWh";
    static final String MINIMUM_CHARGE = "minimumCharge_kWh";
    static final String RECHARGE_RATE_PER_TIME = "rechargeRatePerTime_kW";

    private double dischargeRateByDistance = 0.0;
    private double dischargeRateByTime = 0.0;

    private double maximumCharge = 100.0;
    private double minimumCharge = 0.0;

    private double rechargeRatePerTime = 0.0;

    public StaticChargeCalculatorConfig() {
        super(STATIC_CHARGE_CALCULATOR);
    }

    @StringGetter(DISCHARGE_RATE_BY_DISTANCE)
    public double getDischargeRateByDistance() {
        return dischargeRateByDistance;
    }

    @StringSetter(DISCHARGE_RATE_BY_DISTANCE)
    public void setDischargeRateByDistance(double dischargeRateByDistance) {
        this.dischargeRateByDistance = dischargeRateByDistance;
    }

    @StringGetter(DISCHARGE_RATE_BY_TIME)
    public double getDischargeRateByTime() {
        return dischargeRateByTime;
    }

    @StringSetter(DISCHARGE_RATE_BY_TIME)
    public void setDischargeRateByTime(double dischargeRateByTime) {
        this.dischargeRateByTime = dischargeRateByTime;
    }

    @StringGetter(MAXIMUM_CHARGE)
    public double getMaximumCharge() {
        return maximumCharge;
    }

    @StringSetter(MAXIMUM_CHARGE)
    public void setMaximumCharge(double maximumCharge) {
        this.maximumCharge = maximumCharge;
    }

    @StringGetter(MINIMUM_CHARGE)
    public double getMinimumCharge() {
        return minimumCharge;
    }

    @StringSetter(MINIMUM_CHARGE)
    public void setMinimumCharge(double minimumCharge) {
        this.minimumCharge = minimumCharge;
    }

    @StringGetter(RECHARGE_RATE_PER_TIME)
    public double getRechargeRatePerTime() {
        return rechargeRatePerTime;
    }

    @StringSetter(RECHARGE_RATE_PER_TIME)
    public void setRechargeRatePerTime(double rechargeRatePerTime) {
        this.rechargeRatePerTime = rechargeRatePerTime;
    }
}
