package ch.ethz.matsim.av.electric.calculators;

import ch.ethz.matsim.av.electric.RechargeUtils;
import org.matsim.core.controler.AbstractModule;

public class StaticChargeCalculatorModule extends AbstractModule {
    @Override
    public void install() {
        bind(StaticChargeCalculator.class);
        RechargeUtils.registerChargeCalculator(binder(), "static", StaticChargeCalculator.class);
    }
}
