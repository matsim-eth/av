package ch.ethz.matsim.av.analysis.simulation;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.analysis.FleetDistanceListener;
import ch.ethz.matsim.av.analysis.LinkFinder;
import ch.ethz.matsim.av.analysis.passengers.PassengerAnalysisListener;
import ch.ethz.matsim.av.analysis.passengers.PassengerAnalysisWriter;
import ch.ethz.matsim.av.analysis.vehicles.VehicleAnalysisListener;
import ch.ethz.matsim.av.analysis.vehicles.VehicleAnalysisWriter;
import ch.ethz.matsim.av.config.AVConfigGroup;

@Singleton
public class AnalysisOutputListener implements IterationStartsListener, IterationEndsListener, ShutdownListener {
	private final OutputDirectoryHierarchy outputDirectory;

	private final int passengerAnalysisInterval;
	private final PassengerAnalysisListener passengerAnalysisListener;

	private final int vehicleAnalysisInterval;
	private final VehicleAnalysisListener vehicleAnalysisListener;

	private final boolean enableFleetDistanceListener;
	private final FleetDistanceListener fleetDistanceListener;

	private boolean isPassengerAnalysisActive = false;
	private boolean isVehicleAnalysisActive = false;

	private DistanceAnalysisWriter distanceAnalysisWriter;

	@Inject
	public AnalysisOutputListener(AVConfigGroup config, OutputDirectoryHierarchy outputDirectory, Network network) {
		this.outputDirectory = outputDirectory;

		this.passengerAnalysisInterval = config.getPassengerAnalysisInterval();
		this.vehicleAnalysisInterval = config.getVehicleAnalysisInterval();
		this.enableFleetDistanceListener = config.getEnableDistanceAnalysis();

		LinkFinder linkFinder = new LinkFinder(network);

		this.passengerAnalysisListener = new PassengerAnalysisListener(linkFinder);
		this.vehicleAnalysisListener = new VehicleAnalysisListener(linkFinder);

		this.fleetDistanceListener = new FleetDistanceListener(config.getOperatorConfigs().keySet(), linkFinder);
		this.distanceAnalysisWriter = new DistanceAnalysisWriter(outputDirectory, config.getOperatorConfigs().keySet());
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if (passengerAnalysisInterval > 0 && event.getIteration() % passengerAnalysisInterval == 0) {
			isPassengerAnalysisActive = true;
			event.getServices().getEvents().addHandler(passengerAnalysisListener);
		}

		if (vehicleAnalysisInterval > 0 && event.getIteration() % vehicleAnalysisInterval == 0) {
			isVehicleAnalysisActive = true;
			event.getServices().getEvents().addHandler(vehicleAnalysisListener);
		}

		if (enableFleetDistanceListener) {
			event.getServices().getEvents().addHandler(fleetDistanceListener);
		}
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		try {
			if (isPassengerAnalysisActive) {
				event.getServices().getEvents().removeHandler(passengerAnalysisListener);

				String path = outputDirectory.getIterationFilename(event.getIteration(), "av_passenger_rides.csv");
				new PassengerAnalysisWriter(passengerAnalysisListener).writeRides(new File(path));
			}

			if (isVehicleAnalysisActive) {
				event.getServices().getEvents().removeHandler(vehicleAnalysisListener);

				String movementsPath = outputDirectory.getIterationFilename(event.getIteration(),
						"av_vehicle_movements.csv");
				new VehicleAnalysisWriter(vehicleAnalysisListener).writeMovements(new File(movementsPath));

				String activitiesPath = outputDirectory.getIterationFilename(event.getIteration(),
						"av_vehicle_activities.csv");
				new VehicleAnalysisWriter(vehicleAnalysisListener).writeActivities(new File(activitiesPath));
			}

			if (enableFleetDistanceListener) {
				event.getServices().getEvents().removeHandler(fleetDistanceListener);
				distanceAnalysisWriter.write(fleetDistanceListener);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		try {
			distanceAnalysisWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
