package ch.ethz.matsim.av.generator;


import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;

import java.util.Iterator;

public interface AVGenerator extends Iterator<AVVehicle> {
    interface AVGeneratorFactory {
        AVGenerator createGenerator(AVGeneratorConfig generatorConfig);
    }
}
