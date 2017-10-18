package ch.ethz.matsim.av.electric.assets.vehicle;

import java.util.Map;

import com.google.inject.Inject;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.electric.assets.battery.Battery;
import ch.ethz.matsim.av.electric.assets.battery.BatterySpecification;
import ch.ethz.matsim.av.electric.assets.battery.DefaultBattery;
import ch.ethz.matsim.av.electric.consumption.ConsumptionCalculator;
import ch.ethz.matsim.av.generator.AVGenerator;

public class BatteryVehicleGenerator implements AVGenerator {
	final private AVGenerator delegate;
	final private ConsumptionCalculator consumptionCalculator;
	final private BatterySpecification batterySpecification;
	
	public BatteryVehicleGenerator(AVGenerator delegate, ConsumptionCalculator consumptionCalculator, BatterySpecification batterySpecification) {
		this.delegate = delegate;
		this.consumptionCalculator = consumptionCalculator;
		this.batterySpecification = batterySpecification;
	}

	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	@Override
	public AVVehicle next() {
		AVVehicle delegateVehicle = delegate.next();
		
		Battery battery = new DefaultBattery(batterySpecification);
		battery.setState(batterySpecification.getMaximumEnergy());
		
		return new BatteryVehicle(delegateVehicle.getId(), delegateVehicle.getStartLink(), delegateVehicle.getCapacity(), delegateVehicle.getServiceBeginTime(), delegateVehicle.getServiceEndTime(), battery, consumptionCalculator); 
	}
	
	public static class Factory implements AVGenerator.AVGeneratorFactory {
		@Inject Map<String, AVGenerator.AVGeneratorFactory> generatorFactories;
		
		@Inject BatterySpecification batterySpecification;
		@Inject ConsumptionCalculator consumptionCalculator;
		
		@Override
		public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
			if (!generatorConfig.getParams().containsKey("delegate")) {
				throw new IllegalStateException();
			}
			
			AVGenerator delegate = generatorFactories.get(generatorConfig.getParams().get("delegate")).createGenerator(generatorConfig);
			return new BatteryVehicleGenerator(delegate, consumptionCalculator, batterySpecification);
		}
	}
}
