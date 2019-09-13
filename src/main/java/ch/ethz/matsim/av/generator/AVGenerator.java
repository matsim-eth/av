package ch.ethz.matsim.av.generator;

import java.util.Iterator;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;

public interface AVGenerator extends Iterator<AVVehicle> {
	interface AVGeneratorFactory {
		AVGenerator createGenerator(OperatorConfig operatorConfig);
	}
}
