package ch.ethz.matsim.av.generator;

import java.util.Iterator;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;

public interface AVGenerator extends Iterator<AVVehicle> {
	interface AVGeneratorFactory {
		AVGenerator createGenerator(OperatorConfig operatorConfig, Network network);
	}
}
