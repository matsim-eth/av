package ch.ethz.matsim.av.private_av;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.locationchoice.bestresponse.BackwardDijkstraMultipleDestinations;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.Time;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.generator.AVVehicleCreator;
import ch.ethz.matsim.av.passenger.AVPassengerPickupActivity;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.passenger.OnlineRequestCreator;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

public class PrivateDispatcher implements AVDispatcher {
	final private TravelTime travelTime;
	final private LeastCostPathCalculator forwardRouter;
	final private PrivateScheduleRepository repository;
	final private AVVehicleCreator generator;
	final private OnlineRequestCreator requestCreator;

	final private Map<Id<Person>, DynAgent> vehicles = new HashMap<>();
	
	private boolean initialized = false;
	
	public enum PrivateDispatcherMode {
		STAY_AT_ACTIVITY, RETURN_HOME
	}
	
	final private PrivateDispatcherMode mode;

	public PrivateDispatcher(LeastCostPathCalculator forwardRouter,
			TravelTime travelTime, PrivateScheduleRepository repository, AVVehicleCreator generator,
			OnlineRequestCreator requestCreator, PrivateDispatcherMode mode) {
		this.forwardRouter = forwardRouter;
		this.travelTime = travelTime;
		this.repository = repository;
		this.generator = generator;
		this.requestCreator = requestCreator;
		this.mode = mode;
	}

	@Override
	public void onRequestSubmitted(AVRequest request) {
		DynAgent avAgent = vehicles.get(request.getPassenger().getId());
		DynAction action = avAgent.getCurrentAction();
		
		if (action instanceof AVPassengerPickupActivity) {
			((AVPassengerPickupActivity) action).notifyPassengerIsReadyForDeparture(request.getPassenger(),
					request.getSubmissionTime());
		}
	}

	@Override
	public void onNextTaskStarted(AVVehicle vehicle) {
		// Do nothing
	}

	@Override
	public void onNextTimestep(double now) {
		if (!initialized) {
			initialize();
			initialized = true;
		}
	}

	@Override
	public void addVehicle(AVVehicle vehicle) {
	}

	static private double PICKUP_TIME = 15.0;
	static private double DROPOFF_TIME = 10.0;

	private void initialize() {
		vehicles.clear();
		repository.update();

		for (PrivateSchedule privateSchedule : repository.getSchedules()) {
			Id<Vehicle> vehicleId = Id.create(
					String.format("av_private_%s", privateSchedule.getPerson().getId().toString()), Vehicle.class);
			AVVehicle vehicle = new AVVehicle(vehicleId, privateSchedule.getStartLink(), 4.0, 0.0, 30.0 * 3600.0);
			vehicle.setDispatcher(this);
			vehicles.put(privateSchedule.getPerson().getId(), generator.createVehicle(vehicle));
			
			Schedule schedule = vehicle.getSchedule();

			Link currentLink = privateSchedule.getStartLink();
			double currentTime = privateSchedule.getTrips().iterator().next().getPickupTime();

			int tripIndex = 0;

			for (PrivateSchedule.Trip trip : privateSchedule.getTrips()) {
				AVStayTask lastTask = (AVStayTask) schedule.getTasks().get(schedule.getTaskCount() - 1);
				
				double tripVehicleDistance = 0.0;
				double tripPersonDistance = 0.0;

				// If the vehicle is not at the pickup link, drive there on time
				// This should almost always happen (since the vehicle returns home)

				if (!currentLink.equals(trip.getPickupLink())) {
					VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, trip.getPickupLink(),
							lastTask.getBeginTime(), forwardRouter, travelTime);
					
					double departureTime = Math.max(path.getDepartureTime(),
							trip.getPickupTime() - path.getTravelTime());

					lastTask.setEndTime(departureTime);
					path = new ShiftedPath(path, departureTime, departureTime + path.getTravelTime());

					AVDriveTask pickupDriveTask = new AVDriveTask(path);
					schedule.addTask(pickupDriveTask);

					currentTime = departureTime + path.getTravelTime();
					currentLink = trip.getPickupLink();
					
					tripVehicleDistance += VrpPaths.calcPathDistance(path);
				} else {
					// We're already at the correct link, wait until departure
					lastTask.setEndTime(trip.getPickupTime());
					currentTime = trip.getPickupTime();
				}

				Id<Request> requestId = Id.create(
						String.format("%s_%d", privateSchedule.getPerson().getId().toString(), tripIndex++),
						Request.class);
				AVRequest request = requestCreator.createRequest(requestId, privateSchedule.getPerson().getId(),
						trip.getPickupLink(), trip.getDropoffLink(), trip.getPickupTime(), trip.getRoute());

				// Pick up the owner
				AVPickupTask pickupTask = new AVPickupTask(currentTime, currentTime + PICKUP_TIME, currentLink);
				pickupTask.addRequest(request);
				schedule.addTask(pickupTask);
				currentTime += PICKUP_TIME;

				// Drive him to the destination
				VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, trip.getDropoffLink(), currentTime,
						forwardRouter, travelTime);
				AVDriveTask dropoffDriveTask = new AVDriveTask(path);
				schedule.addTask(dropoffDriveTask);
				
				tripVehicleDistance += VrpPaths.calcPathDistance(path);
				tripPersonDistance += VrpPaths.calcPathDistance(path);

				currentTime += path.getTravelTime();
				currentLink = trip.getDropoffLink();

				// Drop him off
				AVDropoffTask dropoffTask = new AVDropoffTask(currentTime, currentTime + DROPOFF_TIME, currentLink);
				dropoffTask.addRequest(request);
				schedule.addTask(dropoffTask);
				currentTime += DROPOFF_TIME;

				// Either stay there or return home ... 
				if (currentLink != privateSchedule.getHomeLink() && mode.equals(PrivateDispatcherMode.RETURN_HOME)) {
					VrpPathWithTravelData returnPath = VrpPaths.calcAndCreatePath(currentLink,
							privateSchedule.getHomeLink(), currentTime, forwardRouter, travelTime);
					AVDriveTask returnDriveTask = new AVDriveTask(returnPath);
					schedule.addTask(returnDriveTask);

					currentTime += returnPath.getTravelTime();
					currentLink = privateSchedule.getHomeLink();
					
					tripVehicleDistance += VrpPaths.calcPathDistance(returnPath);
				}
				
				request.getRoute().setDistance(tripVehicleDistance);

				AVStayTask stayAtHomeTask = new AVStayTask(currentTime, schedule.getEndTime(), currentLink);
				schedule.addTask(stayAtHomeTask);
			}
		}
	}

	static public class Factory implements AVDispatcher.AVDispatcherFactory {
		@Inject
		@Named(AVModule.AV_MODE)
		public TravelTime travelTime;

		@Inject
		@Named(AVModule.AV_MODE)
		public Network network;

		@Inject
		public Population population;

		@Inject
		public AVVehicleCreator generator;

		@Inject
		public OnlineRequestCreator requestCreator;

		@Override
		public AVDispatcher createDispatcher(AVDispatcherConfig config) {
			LeastCostPathCalculator forwardRouter = new Dijkstra(network,
					new OnlyTimeDependentTravelDisutility(travelTime), travelTime);
			
			PrivateScheduleRepository repository = new PrivateScheduleRepository(population, network, config.getParent().getId().toString());
			
			PrivateDispatcherMode mode = PrivateDispatcherMode.valueOf(config.getParams().getOrDefault("mode", "RETURN_HOME"));

			return new PrivateDispatcher(forwardRouter, travelTime, repository, generator,
					requestCreator, mode);
		}
	}

	static private class ShiftedPath implements VrpPathWithTravelData {
		final private VrpPathWithTravelData delegate;
		final private double departureTime;
		final private double arrivalTime;

		public ShiftedPath(VrpPathWithTravelData delegate, double departureTime, double arrivalTime) {
			this.delegate = delegate;
			this.departureTime = departureTime;
			this.arrivalTime = arrivalTime;
		}

		@Override
		public int getLinkCount() {
			return delegate.getLinkCount();
		}

		@Override
		public Link getLink(int idx) {
			return delegate.getLink(idx);
		}

		@Override
		public double getLinkTravelTime(int idx) {
			return delegate.getLinkTravelTime(idx);
		}

		@Override
		public void setLinkTravelTime(int idx, double linkTT) {
			delegate.setLinkTravelTime(idx, linkTT);
		}

		@Override
		public Link getFromLink() {
			return delegate.getFromLink();
		}

		@Override
		public Link getToLink() {
			return delegate.getToLink();
		}

		@Override
		public Iterator<Link> iterator() {
			return delegate.iterator();
		}

		@Override
		public double getDepartureTime() {
			return departureTime;
		}

		@Override
		public double getTravelTime() {
			return delegate.getTravelTime();
		}

		@Override
		public double getArrivalTime() {
			return arrivalTime;
		}

	}
}
