package ch.ethz.matsim.av.private_av;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.data.Vehicle;

import com.google.inject.Inject;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.generator.AVGenerator;

public class PrivateGenerator implements AVGenerator {
	final private List<AVVehicle> vehicles = new LinkedList<>();

	public PrivateGenerator(Population population) {
		for (Person person : population.getPersons().values()) {
			Id<Vehicle> vehicleId = Id.create(String.format("av_private_%s", schedule.getPerson().getId().toString()),
					Vehicle.class);

			PrivateVehicle vehicle = new PrivateVehicle(vehicleId, schedule.getStartLink(), 4.0, 0.0, 108000.0, null,
					schedule.getHomeLink(), schedule);
			vehicles.add(vehicle);
		}
	}

	@Override
	public boolean hasNext() {
		return vehicles.size() > 0;
	}

	@Override
	public AVVehicle next() {
		return vehicles.remove(0);
	}
	
	static public class Factory implements AVGenerator.AVGeneratorFactory {
		@Inject Population population;

		@Override
		public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
			return new PrivateGenerator(population);
		}
	}
}
