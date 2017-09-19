package ch.ethz.matsim.av.dispatcher.on_demand;

import java.util.LinkedList;
import java.util.Queue;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dynagent.run.DynActivityEngine;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.utils.SingleRideAppender;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.generator.AVVehicleCreator;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public class OnDemandDispatcher implements AVDispatcher {
	final private AVVehicleCreator generator;
	final private SingleRideAppender appender;

	final private Queue<AVRequest> pendingRequests = new LinkedList<>();

	public OnDemandDispatcher(AVVehicleCreator generator, SingleRideAppender appender) {
		this.appender = appender;
		this.generator = generator;
	}

	@Override
	public void onRequestSubmitted(AVRequest request) {
		pendingRequests.add(request);
	}

	@Override
	public void onNextTaskStarted(AVVehicle vehicle) {
		// Not needed here
	}

	private int index = 0;

	@Override
	public void onNextTimestep(double now) {
		appender.update();

		while (pendingRequests.size() > 0) {
			AVRequest request = pendingRequests.poll();
			
			// ATTENTION: Make sure that no reference to those vehicles is 
			// kept since they won't be garbage collected otherwise. The dispatcher
			// itself lives in the Controller scope, not in the QSim scope!

			Id<Vehicle> id = Id.create(String.format("dyn_%d", index++), Vehicle.class);
			AVVehicle vehicle = new AVVehicle(id, request.getFromLink(), 4.0, 0.0, 30.0 * 3600.0);
			vehicle.setDispatcher(this);
			generator.createVehicle(vehicle);

			appender.schedule(request, vehicle, now);
		}
	}

	@Override
	public void addVehicle(AVVehicle vehicle) {
		// This is not needed here!
	}

	@Singleton
	static public class Factory implements AVDispatcher.AVDispatcherFactory {
		@Inject
		AVVehicleCreator generator;

		@Inject
		@Named(AVModule.AV_MODE)
		TravelTime travelTime;

		@Inject
		@Named(AVModule.AV_MODE)
		ParallelLeastCostPathCalculator router;

		@Override
		public AVDispatcher createDispatcher(AVDispatcherConfig config) {
			SingleRideAppender appender = new SingleRideAppender(config, router, travelTime);
			return new OnDemandDispatcher(generator, appender);
		}
	}
}
