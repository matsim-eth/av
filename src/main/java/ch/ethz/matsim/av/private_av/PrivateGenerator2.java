package ch.ethz.matsim.av.private_av;

import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

import com.google.inject.Inject;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.generator.AVGenerator;

public class PrivateGenerator2 { //implements AVGenerator {
	/*final private PrivateScheduleRepository repository;
	final private List<AVVehicle> vehicles = new LinkedList<>();
	
	private boolean initialized = false;

	public PrivateGenerator2(PrivateScheduleRepository repository) {
		this.repository = repository;
	}
	
	private void initialize() {
		repository.update();

		for (PrivateSchedule schedule : repository.getSchedules()) {
			Id<Vehicle> vehicleId = Id.create(String.format("av_private_%s", schedule.getPerson().getId().toString()),
					Vehicle.class);

			PrivateVehicle vehicle = new PrivateVehicle(vehicleId, schedule.getStartLink(), 4.0, 0.0, 108000.0, null,
					schedule.getHomeLink(), schedule);
			vehicles.add(vehicle);
		}
		
		initialized = true;
	}

	@Override
	public boolean hasNext() {
		if (!initialized) initialize();
		return vehicles.size() > 0;
	}

	@Override
	public AVVehicle next() {
		if (!initialized) initialize();
		return vehicles.remove(0);
	}
	
	static public class Factory implements AVGenerator.AVGeneratorFactory {
		@Inject PrivateScheduleRepository repository;

		@Override
		public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
			return new PrivateGenerator2(repository);
		}
	}*/
}
