package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.operator.DispatcherConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregationEvent;
import ch.ethz.matsim.av.dispatcher.single_heuristic.ModeChangeEvent;
import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.schedule.AVTask;

public class MultiODHeuristic implements AVDispatcher {
	public final static String TYPE = "MultiOD";
	
	private boolean reoptimize = true;
	private double nextReplanningTimestamp = 0.0;

	final private Id<AVOperator> operatorId;
	final private EventsManager eventsManager;
	final private double replanningInterval;
	final private long numberOfSeats;

	final private List<AVVehicle> availableVehicles = new LinkedList<>();
	final private List<AggregatedRequest> pendingRequests = new LinkedList<>();
	final private List<AggregatedRequest> assignableRequests = new LinkedList<>();

	final private QuadTree<AVVehicle> availableVehiclesTree;
	final private QuadTree<AggregatedRequest> pendingRequestsTree;

	final private Map<AVVehicle, Link> vehicleLinks = new HashMap<>();
	final private Map<AggregatedRequest, Link> requestLinks = new HashMap<>();

	final private Map<AVVehicle, AggregatedRequest> vehicle2Request = new HashMap<>();

	private SingleHeuristicDispatcher.HeuristicMode mode = SingleHeuristicDispatcher.HeuristicMode.OVERSUPPLY;

	final private AggregateRideAppender appender;
	final private FactorTravelTimeEstimator estimator;

	private double now;

	public MultiODHeuristic(Id<AVOperator> operatorId, EventsManager eventsManager, Network network,
			AggregateRideAppender appender, FactorTravelTimeEstimator estimator, double replanningInterval,
			long numberOfSeats) {
		this.operatorId = operatorId;
		this.eventsManager = eventsManager;
		this.appender = appender;
		this.estimator = estimator;
		this.replanningInterval = replanningInterval;
		this.numberOfSeats = numberOfSeats;

		double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values()); // minx, miny, maxx, maxy

		availableVehiclesTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
		pendingRequestsTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
	}

	@Override
	public void onRequestSubmitted(AVRequest request) {
		addRequest(request, request.getFromLink());
	}

	@Override
	public void onNextTaskStarted(AVVehicle vehicle) {
		AVTask task = (AVTask) vehicle.getSchedule().getCurrentTask();
		if (task.getAVTaskType() == AVTask.AVTaskType.PICKUP) {
			assignableRequests.remove(vehicle2Request.remove(vehicle));
		}

		if (task.getAVTaskType() == AVTask.AVTaskType.STAY) {
			addVehicle(vehicle, ((AVStayTask) task).getLink());
		}
	}

	private void reoptimize(double now) {
		SingleHeuristicDispatcher.HeuristicMode updatedMode = availableVehicles.size() > pendingRequests.size()
				? SingleHeuristicDispatcher.HeuristicMode.OVERSUPPLY
				: SingleHeuristicDispatcher.HeuristicMode.UNDERSUPPLY;

		if (!updatedMode.equals(mode)) {
			mode = updatedMode;
			eventsManager.processEvent(new ModeChangeEvent(mode, operatorId, now));
		}

		while (pendingRequests.size() > 0 && availableVehicles.size() > 0) {
			AggregatedRequest request = null;
			AVVehicle vehicle = null;

			switch (mode) {
			case OVERSUPPLY:
				request = findRequest();
				vehicle = findClosestVehicle(request.getMasterRequest().getFromLink());
				break;
			case UNDERSUPPLY:
				vehicle = findVehicle();
				request = findClosestRequest(vehicleLinks.get(vehicle));
				break;
			}

			removeRequest(request);
			removeVehicle(vehicle);
			vehicle2Request.put(vehicle, request);

			assignableRequests.remove(request); // TODO: IMPORTANT; otherwise REscheduling is necessary!!!
			appender.schedule(request, vehicle, now);
		}
	}

	@Override
	public void onNextTimestep(double now) {
		for (Map.Entry<AVRequest, AVRequest> pair : aggregationMap.entrySet()) {
			eventsManager.processEvent(new AggregationEvent(pair.getValue(), pair.getKey(), now));
		}
		aggregationMap.clear();

		appender.update();

		if (now >= nextReplanningTimestamp) {
			reoptimize = true;
			nextReplanningTimestamp = now + replanningInterval;
		}

		if (reoptimize) {
			reoptimize(now);
			reoptimize = false;
		}
	}

	final private Map<AVRequest, AVRequest> aggregationMap = new HashMap<>();

	private void addRequest(AVRequest request, Link link) {
		AggregatedRequest aggregate = findAggregateRequest(request);

		if (aggregate != null) {
			aggregate.addSlaveRequest(request);
			aggregationMap.put(request, aggregate.getMasterRequest());
		} else {
			aggregate = new AggregatedRequest(request, estimator, numberOfSeats);

			pendingRequests.add(aggregate);
			assignableRequests.add(aggregate);
			requestLinks.put(aggregate, link);
			pendingRequestsTree.put(link.getCoord().getX(), link.getCoord().getY(), aggregate);
			// reoptimize = true;
		}
	}

	private AggregatedRequest findAggregateRequest(AVRequest request) {
		AggregatedRequest bestAggregate = null;
		double bestCost = Double.POSITIVE_INFINITY;

		for (AggregatedRequest candidate : assignableRequests) {
			if (candidate == null)
				throw new IllegalStateException();
			Double cost = candidate.accept(request);

			if (cost != null && cost < bestCost) {
				bestCost = cost;
				bestAggregate = candidate;
			}
		}

		return bestAggregate;
	}

	private AggregatedRequest findRequest() {
		return pendingRequests.get(0);
	}

	private AVVehicle findVehicle() {
		return availableVehicles.get(0);
	}

	private AVVehicle findClosestVehicle(Link link) {
		Coord coord = link.getCoord();
		return availableVehiclesTree.getClosest(coord.getX(), coord.getY());
	}

	private AggregatedRequest findClosestRequest(Link link) {
		Coord coord = link.getCoord();
		return pendingRequestsTree.getClosest(coord.getX(), coord.getY());
	}

	@Override
	public void addVehicle(AVVehicle vehicle) {
		addVehicle(vehicle, vehicle.getStartLink());
		eventsManager.processEvent(new AVVehicleAssignmentEvent(vehicle, 0));
	}

	private void addVehicle(AVVehicle vehicle, Link link) {
		availableVehicles.add(vehicle);
		availableVehiclesTree.put(link.getCoord().getX(), link.getCoord().getY(), vehicle);
		vehicleLinks.put(vehicle, link);
		// reoptimize = true;
	}

	private void removeVehicle(AVVehicle vehicle) {
		availableVehicles.remove(vehicle);
		Coord coord = vehicleLinks.remove(vehicle).getCoord();
		availableVehiclesTree.remove(coord.getX(), coord.getY(), vehicle);
	}

	private void removeRequest(AggregatedRequest request) {
		pendingRequests.remove(request);
		Coord coord = requestLinks.remove(request).getCoord();
		pendingRequestsTree.remove(coord.getX(), coord.getY(), request);
	}

	static public class Factory implements AVDispatcherFactory {
		@Inject
		@Named(AVModule.AV_MODE)
		private Network network;

		@Inject
		private EventsManager eventsManager;

		@Inject
		@Named(AVModule.AV_MODE)
		private LeastCostPathCalculator router;

		@Inject
		@Named(AVModule.AV_MODE)
		private TravelTime travelTime;

		@Override
		public AVDispatcher createDispatcher(OperatorConfig operatorConfig, AVRouter parallelRouter) {
			DispatcherConfig dispatcherConfig = operatorConfig.getDispatcherConfig();

			double replanningInterval = Double
					.parseDouble(dispatcherConfig.getParams().getOrDefault("replanningInterval", "10.0"));
			double threshold = Double
					.parseDouble(dispatcherConfig.getParams().getOrDefault("maximumTimeRadius", "600.0"));
			boolean useParallelImplementation = Boolean
					.parseBoolean(dispatcherConfig.getParams().getOrDefault("useParallelImplementation", "true"));
			long numberOfSeats = Long
					.parseLong(operatorConfig.getGeneratorConfig().getParams().getOrDefault("numberOfSeats", "4"));

			FactorTravelTimeEstimator estimator = new FactorTravelTimeEstimator(threshold);

			return new MultiODHeuristic(operatorConfig.getId(), eventsManager, network, useParallelImplementation
					? new ParallelAggregateRideAppender(operatorConfig.getTimingConfig(), parallelRouter, travelTime,
							estimator)
					: new SerialAggregateRideAppender(operatorConfig.getTimingConfig(), router, travelTime, estimator),
					estimator, replanningInterval, numberOfSeats);
		}
	}
}
