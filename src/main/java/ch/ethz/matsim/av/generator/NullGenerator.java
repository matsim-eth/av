package ch.ethz.matsim.av.generator;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;

public class NullGenerator implements AVGenerator {
	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public AVVehicle next() {
		return null;
	}
	
	public static class Factory implements AVGenerator.AVGeneratorFactory {
		@Override
		public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
			return new NullGenerator();
		}
	}
}
