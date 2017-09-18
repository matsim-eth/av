package ch.ethz.matsim.av.private_av;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.path.VrpPath;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.locationchoice.bestresponse.BackwardDijkstraMultipleDestinations;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

public class PrivateDispatcher implements AVDispatcher {
	final private TravelTime travelTime;
	final private LeastCostPathCalculator forwardRouter;
	final private LeastCostPathCalculator backwardRouter;

	public PrivateDispatcher(LeastCostPathCalculator forwardRouter, LeastCostPathCalculator backwardRouter, TravelTime travelTime) {
		this.forwardRouter = forwardRouter;
		this.backwardRouter = backwardRouter;
		this.travelTime = travelTime;
	}
	
	@Override
	public void onRequestSubmitted(AVRequest request) {
		// Do nothing
	}

	@Override
	public void onNextTaskStarted(AVVehicle vehicle) {
		// Do nothing
	}

	@Override
	public void onNextTimestep(double now) {
		// Do nothing
	}

	@Override
	public void addVehicle(AVVehicle vehicle) {
		constructSchedule((PrivateVehicle) vehicle);
	}

	static private double PICKUP_TIME = 15.0;
	static private double DROPOFF_TIME = 10.0;

	private void constructSchedule(PrivateVehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();

		Link currentLink = vehicle.getPrivateSchedule().getStartLink();
		double currentTime = vehicle.getPrivateSchedule().getTrips().iterator().next().getPickupTime();

		for (PrivateSchedule.Trip trip : vehicle.getPrivateSchedule().getTrips()) {
			AVStayTask lastTask = (AVStayTask) schedule.getTasks().get(schedule.getTaskCount() - 1);

			// If the vehicle is not at the pickup link, drive there on time
			// This should almost always happen (since the vehiclce returns home)
			if (!currentLink.equals(trip.getPickupLink())) {
				VrpPathWithTravelData path = constructBackwardPath(currentLink, trip.getPickupLink(),
						trip.getPickupTime());
				double optimalDepartureTime = trip.getPickupTime() - path.getTravelTime();

				if (optimalDepartureTime < lastTask.getBeginTime()) {
					// We cannot be on time, since the vehicle is not available to depart then
					lastTask.setEndTime(lastTask.getEndTime());
					currentTime = lastTask.getBeginTime();
				} else {
					// We can be on time, let the vehicle rest for a while
					lastTask.setEndTime(optimalDepartureTime);
					currentTime = optimalDepartureTime;
				}

				AVDriveTask pickupDriveTask = new AVDriveTask(path);
				schedule.addTask(pickupDriveTask);

				currentTime += path.getTravelTime();
				currentLink = trip.getPickupLink();
			} else {
				// We're already at the correct link, wait until departure
				lastTask.setEndTime(trip.getPickupTime());
				currentTime = trip.getPickupTime();
			}

			// Pick up the owner
			AVPickupTask pickupTask = new AVPickupTask(currentTime, currentTime + PICKUP_TIME, currentLink);
			schedule.addTask(pickupTask);
			currentTime += PICKUP_TIME;

			// Drive him to the destination
			VrpPathWithTravelData path = constructForwardPath(trip.getPickupTime(), currentLink, trip.getDropoffLink());
			AVDriveTask dropoffDriveTask = new AVDriveTask(path);
			schedule.addTask(dropoffDriveTask);

			currentTime += path.getTravelTime();
			currentLink = trip.getDropoffLink();

			// Drop him off
			AVDropoffTask dropoffTask = new AVDropoffTask(currentTime, currentTime + DROPOFF_TIME, currentLink);
			schedule.addTask(dropoffTask);
			currentTime += DROPOFF_TIME;

			// What happens after?
			// Here we send the AV home...
			// may be there should be exceptions when it is clear that
			// it will take to long to go home until the next time the
			// vehicle is needed? (TODO)

			VrpPathWithTravelData returnPath = constructForwardPath(currentTime, currentLink, vehicle.getHomeLink());
			AVDriveTask returnDriveTask = new AVDriveTask(returnPath);
			schedule.addTask(returnDriveTask);

			currentTime += path.getTravelTime();
			currentLink = vehicle.getHomeLink();

			AVStayTask stayAtHomeTask = new AVStayTask(currentTime, schedule.getEndTime(), currentLink);
			schedule.addTask(stayAtHomeTask);
		}
	}

	private VrpPathWithTravelData constructForwardPath(double departureTime, Link originLink, Link destinationLink) {
		return VrpPaths.calcAndCreatePath(originLink, destinationLink, departureTime, forwardRouter, travelTime);
	}

	private VrpPathWithTravelData constructBackwardPath(Link originLink, Link destinationLink, double arrivalTime) {
		return VrpPaths.calcAndCreatePath(originLink, destinationLink, arrivalTime, backwardRouter, travelTime);
	}
	
	static public class Factory implements AVDispatcher.AVDispatcherFactory {
		@Inject @Named(AVModule.AV_MODE)
		public TravelTime travelTime;
		
		@Inject @Named(AVModule.AV_MODE)
		public Network network;
		
		@Override
		public AVDispatcher createDispatcher(AVDispatcherConfig config) {
			LeastCostPathCalculator forwardRouter = new Dijkstra(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);
			LeastCostPathCalculator backwardRouter = new BackwardDijkstraMultipleDestinations(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);
			
			return new PrivateDispatcher(forwardRouter, backwardRouter, travelTime);
		}
	}
}
