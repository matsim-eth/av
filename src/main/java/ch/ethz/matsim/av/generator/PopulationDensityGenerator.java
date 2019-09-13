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

import com.google.inject.Inject;

import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;

public class PopulationDensityGenerator implements AVGenerator {
	static public final String TYPE = "PopulationDensity";

	private final Random random;
	private final long numberOfVehicles;
	private final int numberOfSeats;
	private long generatedNumberOfVehicles = 0;

	private final String prefix;

	private List<Link> linkList = new LinkedList<>();
	private Map<Link, Double> cumulativeDensity = new HashMap<>();

	public PopulationDensityGenerator(String prefix, int numberOfVehicles, Network network, Population population,
			ActivityFacilities facilities, int randomSeed, int numberOfSeats) {
		this.random = new Random(randomSeed);
		this.numberOfVehicles = numberOfVehicles;
		this.numberOfSeats = numberOfSeats;
		this.prefix = prefix;

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

		Id<DvrpVehicle> id = Id.create("av_" + prefix + String.valueOf(generatedNumberOfVehicles), DvrpVehicle.class);
		return new AVVehicle(id, selectedLink, numberOfSeats + 1, 0.0, Double.POSITIVE_INFINITY);
	}

	static public class Factory implements AVGenerator.AVGeneratorFactory {
		@Inject
		private Population population;

		@Inject
		private ActivityFacilities facilities;

		@Override
		public AVGenerator createGenerator(OperatorConfig operatorConfig, Network network) {
			GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();

			String prefix = "av_" + operatorConfig.getId().toString() + "_";
			int randomSeed = Integer.parseInt(generatorConfig.getParams().getOrDefault("randomSeed", "1234"));
			int numberOfSeats = Integer.parseInt(generatorConfig.getParams().getOrDefault("numberOfSeats", "4"));

			return new PopulationDensityGenerator(prefix, generatorConfig.getNumberOfVehicles(), network, population,
					facilities, randomSeed, numberOfSeats);
		}
	}
}
