package ch.ethz.matsim.av.analysis.run;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import ch.ethz.matsim.av.analysis.LinkFinder;
import ch.ethz.matsim.av.analysis.vehicles.VehicleAnalysisListener;
import ch.ethz.matsim.av.analysis.vehicles.VehicleAnalysisWriter;

public class RunVehicleAnalysis {
	static public void main(String[] args) throws IOException {
		String eventsPath = args[0];
		String networkPath = args[1];
		String movementsOutputPath = args[2];
		String activitiesOutputPath = args[3];

		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkPath);

		LinkFinder linkFinder = new LinkFinder(network);
		VehicleAnalysisListener listener = new VehicleAnalysisListener(linkFinder);

		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(listener);
		new MatsimEventsReader(eventsManager).readFile(eventsPath);

		new VehicleAnalysisWriter(listener).writeMovements(new File(movementsOutputPath));
		new VehicleAnalysisWriter(listener).writeActivities(new File(activitiesOutputPath));
	}
}
