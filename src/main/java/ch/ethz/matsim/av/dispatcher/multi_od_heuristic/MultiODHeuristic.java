package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
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
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.schedule.AVTask;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultiODHeuristic implements AVDispatcher {
    private boolean reoptimize = true;

    final private Id<AVOperator> operatorId;
    final private EventsManager eventsManager;

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

    final private Map<Long, Long> shareHistogram = new HashMap<>();

    private double now;

    public MultiODHeuristic(Id<AVOperator> operatorId, EventsManager eventsManager, Network network, AggregateRideAppender appender, FactorTravelTimeEstimator estimator) {
        this.operatorId = operatorId;
        this.eventsManager = eventsManager;
        this.appender = appender;
        this.estimator = estimator;

        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values()); // minx, miny, maxx, maxy

        availableVehiclesTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
        pendingRequestsTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);

        shareHistogram.put(new Long(1), new Long(0));
        shareHistogram.put(new Long(2), new Long(0));
        shareHistogram.put(new Long(3), new Long(0));
        shareHistogram.put(new Long(4), new Long(0));
    }

    @Override
    public void onRequestSubmitted(AVRequest request) {
        addRequest(request, request.getFromLink());
    }

    @Override
    public void onNextTaskStarted(AVVehicle vehicle) {
    	AVTask task = (AVTask)vehicle.getSchedule().getCurrentTask();
        if (task.getAVTaskType() == AVTask.AVTaskType.PICKUP) {
            assignableRequests.remove(vehicle2Request.remove(vehicle));
        }

        if (task.getAVTaskType() == AVTask.AVTaskType.STAY) {
            addVehicle(vehicle, ((AVStayTask) task).getLink());
        }
    }

    private void reoptimize(double now) {
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

            long count = request.getSlaveRequests().size() + 1;
            shareHistogram.put(count, shareHistogram.get(count) + 1);
        }

        SingleHeuristicDispatcher.HeuristicMode updatedMode =
                availableVehicles.size() > 0 ?
                        SingleHeuristicDispatcher.HeuristicMode.OVERSUPPLY :
                        SingleHeuristicDispatcher.HeuristicMode.UNDERSUPPLY;

        if (!updatedMode.equals(mode)) {
            mode = updatedMode;
            eventsManager.processEvent(new ModeChangeEvent(mode, operatorId, now));
        }
    }

    @Override
    public void onNextTimestep(double now) {
        appender.update();
        this.now = now;
        if (reoptimize) reoptimize(now);
    }

    private void addRequest(AVRequest request, Link link) {
        AggregatedRequest aggregate = findAggregateRequest(request);

        if (aggregate != null) {
            aggregate.addSlaveRequest(request);
            eventsManager.processEvent(new AggregationEvent(aggregate.getMasterRequest(), request, now));
        } else {
            aggregate = new AggregatedRequest(request, estimator);

            pendingRequests.add(aggregate);
            assignableRequests.add(aggregate);
            requestLinks.put(aggregate, link);
            pendingRequestsTree.put(link.getCoord().getX(), link.getCoord().getY(), aggregate);
        }
    }

    private AggregatedRequest findAggregateRequest(AVRequest request) {
        AggregatedRequest bestAggregate = null;
        double bestCost = Double.POSITIVE_INFINITY;

        for (AggregatedRequest candidate : assignableRequests) {
            if (candidate == null) throw new IllegalStateException();
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
        reoptimize = true;
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
        @Inject @Named(AVModule.AV_MODE)
        private Network network;

        @Inject private EventsManager eventsManager;

        @Inject @Named(AVModule.AV_MODE)
        private LeastCostPathCalculator router;

        @Inject @Named(AVModule.AV_MODE)
        private ParallelLeastCostPathCalculator parallelRouter;

        @Inject @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig config) {
            double threshold = Double.parseDouble(config.getParams().getOrDefault("maximumTimeRadius", "600.0"));
            boolean useParallelImplementation = Boolean.parseBoolean(config.getParams().getOrDefault("useParallelImplementation", "true"));

            FactorTravelTimeEstimator estimator = new FactorTravelTimeEstimator(threshold);

            return new MultiODHeuristic(
                    config.getParent().getId(),
                    eventsManager,
                    network,
                    useParallelImplementation ?
                            new ParallelAggregateRideAppender(config, parallelRouter, travelTime, estimator) :
                            new SerialAggregateRideAppender(config, router, travelTime, estimator),
                    estimator
            );
        }
    }
}
