package ch.ethz.matsim.av.electric;

import ch.ethz.matsim.av.electric.calculators.ChargeCalculator;
import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

public class RechargeUtils {
    static public LinkedBindingBuilder<ChargeCalculator> bindChargeCalculator(Binder binder, String calculatorName) {
        MapBinder<String, ChargeCalculator> map = MapBinder.newMapBinder(
                binder, String.class, ChargeCalculator.class);
        return map.addBinding(calculatorName);
    }

    static public void registerChargeCalculator(Binder binder, String calculatorName, Class<? extends ChargeCalculator> clazz) {
        bindChargeCalculator(binder, calculatorName).to(clazz);
    }
}
