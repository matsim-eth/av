package ch.ethz.matsim.av.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.facilities.ActivityFacilities;
import org.opengis.feature.simple.SimpleFeature;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.framework.AVModule;

public class PopulationDensityGenerator implements AVGenerator {
	private final Random random;
	private final long numberOfVehicles;
	private final int numberOfSeats;
	private long generatedNumberOfVehicles = 0;

	private final String prefix;

	private List<Link> linkList = new LinkedList<>();
	private Map<Link, Double> cumulativeDensity = new HashMap<>();

	public PopulationDensityGenerator(AVGeneratorConfig config, Network network, Population population,
			ActivityFacilities facilities, int randomSeed, int numberOfSeats) {
		this.random = new Random(randomSeed);
		this.numberOfVehicles = config.getNumberOfVehicles();
		this.numberOfSeats = numberOfSeats;
		final CoordAnalyzer coordAnalyzer = new CoordAnalyzer(config.getPathToSHP(), network);

		String prefix = config.getPrefix();
		this.prefix = prefix == null ? "av_" + config.getParent().getId().toString() + "_" : prefix + "_";

		// Determine density
		double sum = 0.0;
		Map<Link, Double> density = new HashMap<>();

		for (Person person : population.getPersons().values()) {
			Activity act = (Activity) person.getSelectedPlan().getPlanElements().get(0);
			Id<Link> linkId = act.getLinkId() != null ? act.getLinkId()
					: facilities.getFacilities().get(act.getFacilityId()).getLinkId();
			Link link = network.getLinks().get(linkId);

			if (link != null) {
				if (coordAnalyzer.isLinkInArea(link)) {
					if (density.containsKey(link)) {
						density.put(link, density.get(link) + 1.0);
					} else {
						density.put(link, 1.0);
					}

					if (!linkList.contains(link))
						linkList.add(link);
					sum += 1.0;
				}
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
		@Named(AVModule.AV_MODE)
		private Network network;
		@Inject
		private ActivityFacilities facilities;

		@Override
		public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
			int randomSeed = Integer.parseInt(generatorConfig.getParams().getOrDefault("randomSeed", "1234"));
			int numberOfSeats = Integer.parseInt(generatorConfig.getParams().getOrDefault("numberOfSeats", "4"));
			return new PopulationDensityGenerator(generatorConfig, network, population, facilities, randomSeed,
					numberOfSeats);
		}
	}

	static private class CoordAnalyzer {
		private final Geometry area;
		private final GeometryFactory factory;
		private final Map<Id, Boolean> linkCache;

		private CoordAnalyzer(String pathToSHP, Network network) {
			this.linkCache = new HashMap<>();
			if (pathToSHP != null) {
				Set<SimpleFeature> features = new HashSet<>();
				features.addAll(ShapeFileReader.getAllFeatures(pathToSHP));
				this.area = mergeGeometries(features);
				this.factory = new GeometryFactory();
			} else {
				for (Id<Link> linkId : network.getLinks().keySet()) {
					this.linkCache.put(linkId, true);
				}
				this.area = null;
				this.factory = null;
			}
		}

		boolean isLinkInArea(Link link) {
			Boolean inArea = linkCache.get(link.getId());
			if (inArea == null) {
				inArea = area
						.contains(factory.createPoint(new Coordinate(link.getCoord().getX(), link.getCoord().getY())));
				linkCache.put(link.getId(), inArea);
			}
			return inArea;
		}

		private Geometry mergeGeometries(Set<SimpleFeature> features) {
			Geometry geometry = null;
			for (SimpleFeature feature : features) {
				if (geometry == null) {
					geometry = (Geometry) ((Geometry) feature.getDefaultGeometry()).clone();
				} else {
					geometry = geometry.union((Geometry) feature.getDefaultGeometry());
				}
			}
			return geometry;
		}
	}
}
