package ch.ethz.matsim.av.generator;

import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.framework.AVModule;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.facilities.ActivityFacilities;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;

public class PopulationDensityGenerator implements AVGenerator {
    private final long numberOfVehicles;
	private long generatedNumberOfVehicles = 0;

    private final String prefix;

    private List<Link> linkList = new LinkedList<>();
    private Map<Link, Double> cumulativeDensity = new HashMap<>();

    public PopulationDensityGenerator(AVGeneratorConfig config, Network network, Population population,
									  ActivityFacilities facilities) {
        this.numberOfVehicles = config.getNumberOfVehicles();

        String prefix = config.getPrefix();
        this.prefix = prefix == null ? "av_" + config.getParent().getId().toString() + "_" : prefix + "_";

        // Determine density
        double sum = 0.0;
        Map<Link, Double> density = new HashMap<>();

        for (Person person : population.getPersons().values()) {
			Activity act = (Activity) person.getSelectedPlan().getPlanElements().get(0);
			Id<Link> linkId = act.getLinkId() != null ? act.getLinkId() :
					facilities.getFacilities().get(act.getFacilityId()).getLinkId();
			Link link = network.getLinks().get(linkId);
			
			if (link != null) {
				if (density.containsKey(link)) {
					density.put(link, density.get(link) + 1.0);
				} else {
					density.put(link, 1.0);
				}

				if (!linkList.contains(link)) linkList.add(link);
				sum += 1.0;
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
        double r = MatsimRandom.getRandom().nextDouble();
        Link selectedLink = linkList.get(0);

        for (Link link : linkList) {
            if (r <= cumulativeDensity.get(link)) {
                selectedLink = link;
                break;
            }
        }

        Id<Vehicle> id = Id.create("av_" + prefix + String.valueOf(generatedNumberOfVehicles), Vehicle.class);
        return new AVVehicle(id, selectedLink, 4.0, 0.0, 108000.0);
    }

    static public class Factory implements AVGenerator.AVGeneratorFactory {
        @Inject private Population population;
        @Inject @Named(AVModule.AV_MODE) private Network network;
        @Inject private ActivityFacilities facilities;

        @Override
        public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
            return new PopulationDensityGenerator(generatorConfig, network, population, facilities);
        }
    }
}
