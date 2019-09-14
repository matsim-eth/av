package ch.ethz.matsim.av.generator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.Vehicles;

import com.google.inject.Inject;

import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;

public class PopulationDensityGenerator implements AVGenerator {
	static public final String TYPE = "PopulationDensity";

	private final Random random;
	private final long numberOfVehicles;
	private final VehicleType vehicleType;
	private long generatedNumberOfVehicles = 0;

	private final Id<AVOperator> operatorId;

	private List<Link> linkList = new LinkedList<>();
	private Map<Link, Double> cumulativeDensity = new HashMap<>();

	public PopulationDensityGenerator(Id<AVOperator> operatorId, int numberOfVehicles, Network network,
			Population population, ActivityFacilities facilities, int randomSeed, VehicleType vehicleType) {
		this.random = new Random(randomSeed);
		this.numberOfVehicles = numberOfVehicles;
		this.vehicleType = vehicleType;
		this.operatorId = operatorId;

		// Determine density
		double sum = 0.0;
		Map<Link, Double> density = new HashMap<>();

		for (Person person : population.getPersons().values()) {
			Activity act = (Activity) person.getSelectedPlan().getPlanElements().get(0);
			Id<Link> linkId = act.getLinkId() != null ? act.getLinkId()
					: facilities.getFacilities().get(act.getFacilityId()).getLinkId();
			Link link = network.getLinks().get(linkId);

			if (link != null) {
				if (density.containsKey(link)) {
					density.put(link, density.get(link) + 1.0);
				} else {
					density.put(link, 1.0);
				}

				if (!linkList.contains(link))
					linkList.add(link);
				sum += 1.0; // TODO This looks strange!
			}
		}

		// Compute relative frequencies and cumulative
		double cumsum = 0.0;

		for (Link link : linkList) {
			cumsum += density.get(link) / sum;
			cumulativeDensity.put(link, cumsum);
		}
	}

	@Override
	public boolean hasNext() {
		return generatedNumberOfVehicles < numberOfVehicles;
	}

	@Override
	public AVVehicle next() {
		generatedNumberOfVehicles++;

		// Multinomial selection
		double r = random.nextDouble();
		Link selectedLink = linkList.get(0);

		for (Link link : linkList) {
			if (r <= cumulativeDensity.get(link)) {
				selectedLink = link;
				break;
			}
		}

		Id<DvrpVehicle> id = AVUtils.createId(operatorId, generatedNumberOfVehicles);
		return new AVVehicle(id, selectedLink, 0.0, Double.POSITIVE_INFINITY, vehicleType);
	}

	static public class Factory implements AVGenerator.AVGeneratorFactory {
		@Inject
		private Population population;

		@Inject
		private ActivityFacilities facilities;

		@Inject
		private Vehicles vehicles;

		@Override
		public AVGenerator createGenerator(OperatorConfig operatorConfig, Network network) {
			GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();

			VehicleType vehicleType = VehicleUtils.getDefaultVehicleType();

			if (generatorConfig.getVehicleType() != null) {
				vehicleType = vehicles.getVehicleTypes()
						.get(Id.create(generatorConfig.getVehicleType(), VehicleType.class));

				if (vehicleType == null) {
					throw new IllegalStateException(String.format("VehicleType '%s' does not exist for operator '%s'",
							vehicleType, operatorConfig.getId()));
				}
			}

			int randomSeed = Integer.parseInt(generatorConfig.getParams().getOrDefault("randomSeed", "1234"));

			return new PopulationDensityGenerator(operatorConfig.getId(), generatorConfig.getNumberOfVehicles(),
					network, population, facilities, randomSeed, vehicleType);
		}
	}
}
