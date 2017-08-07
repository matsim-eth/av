package ch.ethz.matsim.av.electric.calculators;

import ch.ethz.matsim.av.electric.RechargeUtils;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;

public class BinnedChargeCalculatorModule extends AbstractModule {
    final private Logger logger = Logger.getLogger(BinnedChargeCalculatorModule.class);

    @Override
    public void install() {
        bind(BinnedChargeCalculator.class);
        RechargeUtils.registerChargeCalculator(binder(), "binned", BinnedChargeCalculator.class);
    }

    @Provides
    @Singleton
    private BinnedChargeCalculatorData provideBinnedChargeCalculatorData(Config config, BinnedChargeCalculatorConfig chargeConfig) {
        VariableBinSizeData data = new VariableBinSizeData();

        if (chargeConfig.getInputPath() == null) {
            logger.warn("No data defined");
            return new VariableBinSizeData();
        }

        BinnedChargeDataReader reader = new BinnedChargeDataReader(data);
        reader.readFile(ConfigGroup.getInputFileURL(config.getContext(), chargeConfig.getInputPath()).getPath());

        if (data.hasFixedIntervals()) {
            logger.info("Data uses fixed bin sizes");
            return FixedBinSizeData.createFromVariableData(data);
        } else {
            logger.warn("Data does not have fixed bin sizes. Consider fixing them for improved performance.");
            return data;
        }
    }
}
