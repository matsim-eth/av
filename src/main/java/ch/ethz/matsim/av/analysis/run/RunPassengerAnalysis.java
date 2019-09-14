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
import ch.ethz.matsim.av.analysis.passengers.PassengerAnalysisListener;
import ch.ethz.matsim.av.analysis.passengers.PassengerAnalysisWriter;

public class RunPassengerAnalysis {
	static public void main(String[] args) throws IOException {
		String eventsPath = args[0];
		String networkPath = args[1];
		String outputPath = args[2];

		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkPath);

		LinkFinder linkFinder = new LinkFinder(network);
		PassengerAnalysisListener listener = new PassengerAnalysisListener(linkFinder);

		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(listener);
		new MatsimEventsReader(eventsManager).readFile(eventsPath);

		new PassengerAnalysisWriter(listener).writeRides(new File(outputPath));
	}
}
