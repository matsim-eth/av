package ch.ethz.matsim.av.router;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public interface AVRouter extends ParallelLeastCostPathCalculator {
	interface Factory {
		AVRouter createRouter();
	}
}
