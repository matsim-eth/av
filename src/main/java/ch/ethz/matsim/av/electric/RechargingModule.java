package ch.ethz.matsim.av.electric;

import ch.ethz.matsim.av.electric.calculators.ChargeCalculator;
import ch.ethz.matsim.av.electric.calculators.StaticChargeCalculator;
import ch.ethz.matsim.av.electric.logic.RechargingDispatcher;
import ch.ethz.matsim.av.electric.tracker.CSVConsumptionTracker;
import ch.ethz.matsim.av.electric.tracker.ConsumptionTracker;
import ch.ethz.matsim.av.electric.tracker.NullConsumptionTracker;
import ch.ethz.matsim.av.extern.BinCalculator;
import ch.ethz.matsim.av.framework.AVUtils;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.matsim.core.controler.AbstractModule;

public class RechargingModule extends AbstractModule {
    final private Logger logger = Logger.getLogger(RechargingModule.class);

    @Override
    public void install() {
        bind(ChargeCalculator.class).to(StaticChargeCalculator.class).asEagerSingleton();
        AVUtils.registerDispatcherFactory(binder(), "Recharging", RechargingDispatcher.Factory.class);

        RechargingConfig config = (RechargingConfig)getConfig().getModules().get(RechargingConfig.RECHARGING);

        if (config.getTrackConsumption()) {
            logger.info("EAV Consumption tracking enabled");
            bind(ConsumptionTracker.class).to(CSVConsumptionTracker.class);
            addControlerListenerBinding().to(CSVConsumptionTracker.class);
        } else {
            logger.info("EAV Consumption tracking disabled");
            bind(ConsumptionTracker.class).toInstance(new NullConsumptionTracker());
        }
    }

    @Provides @Singleton
    private CSVConsumptionTracker provideCSVConsumptionTracker(RechargingConfig rechargingConfig) {
        if (rechargingConfig.getTrackingEndTime() <= rechargingConfig.getTrackingStartTime()) {
            throw new IllegalArgumentException("Tracking end time must be larger than start time");
        }

        if (!((rechargingConfig.getTrackingEndTime() - rechargingConfig.getTrackingStartTime()) % rechargingConfig.getTrackingBinDuration() == 0)) {
            throw new IllegalArgumentException();
        }

        BinCalculator binCalculator = BinCalculator.createByInterval(rechargingConfig.getTrackingStartTime(), rechargingConfig.getTrackingEndTime(), rechargingConfig.getTrackingBinDuration());
        return new CSVConsumptionTracker(binCalculator);
    }
}
