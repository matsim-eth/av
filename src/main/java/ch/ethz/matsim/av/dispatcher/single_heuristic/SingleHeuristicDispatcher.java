package ch.ethz.matsim.av.dispatcher.single_heuristic;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.dispatcher.utils.SingleRideAppender;
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
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SingleHeuristicDispatcher implements AVDispatcher {
    private boolean reoptimize = true;

    final private SingleRideAppender appender;
    final private Id<AVOperator> operatorId;
    final private EventsManager eventsManager;

    final private List<AVVehicle> availableVehicles = new LinkedList<>();
    final private List<AVRequest> pendingRequests = new LinkedList<>();

    final private QuadTree<AVVehicle> availableVehiclesTree;
    final private QuadTree<AVRequest> pendingRequestsTree;

    final private Map<AVVehicle, Link> vehicleLinks = new HashMap<>();
    final private Map<AVRequest, Link> requestLinks = new HashMap<>();

    public enum HeuristicMode {
        OVERSUPPLY, UNDERSUPPLY
    }

    private HeuristicMode mode = HeuristicMode.OVERSUPPLY;

    public SingleHeuristicDispatcher(Id<AVOperator> operatorId, EventsManager eventsManager, Network network, SingleRideAppender appender) {
        this.appender = appender;
        this.operatorId = operatorId;
        this.eventsManager = eventsManager;

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
    	AVTask task = (AVTask)vehicle.getSchedule().getCurrentTask();
        if (task.getAVTaskType() == AVTask.AVTaskType.STAY) {
            addVehicle(vehicle, ((AVStayTask) task).getLink());
        }
    }

    private void reoptimize(double now) {
        while (pendingRequests.size() > 0 && availableVehicles.size() > 0) {
            AVRequest request = null;
            AVVehicle vehicle = null;

            switch (mode) {
                case OVERSUPPLY:
                    request = findRequest();
                    vehicle = findClosestVehicle(request.getFromLink());
                    break;
                case UNDERSUPPLY:
                    vehicle = findVehicle();
                    request = findClosestRequest(vehicleLinks.get(vehicle));
                    break;
            }

            removeRequest(request);
            removeVehicle(vehicle);

            appender.schedule(request, vehicle, now);
        }

        HeuristicMode updatedMode = availableVehicles.size() > 0 ? HeuristicMode.OVERSUPPLY : HeuristicMode.UNDERSUPPLY;
        if (!updatedMode.equals(mode)) {
            mode = updatedMode;
            eventsManager.processEvent(new ModeChangeEvent(mode, operatorId, now));
        }
    }

    @Override
    public void onNextTimestep(double now) {
        appender.update();
        if (reoptimize) reoptimize(now);
    }

    private void addRequest(AVRequest request, Link link) {
        pendingRequests.add(request);
        pendingRequestsTree.put(link.getCoord().getX(), link.getCoord().getY(), request);
        requestLinks.put(request, link);
        reoptimize = true;
    }

    private AVRequest findRequest() {
        return pendingRequests.get(0);
    }

    private AVVehicle findVehicle() {
        return availableVehicles.get(0);
    }

    private AVVehicle findClosestVehicle(Link link) {
        Coord coord = link.getCoord();
        return availableVehiclesTree.getClosest(coord.getX(), coord.getY());
    }

    private AVRequest findClosestRequest(Link link) {
        Coord coord = link.getCoord();
        return pendingRequestsTree.getClosest(coord.getX(), coord.getY());
    }

    @Override
    public void addVehicle(AVVehicle vehicle) {
        eventsManager.processEvent(new AVVehicleAssignmentEvent(vehicle, 0));
        addVehicle(vehicle, vehicle.getStartLink());
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

    private void removeRequest(AVRequest request) {
        pendingRequests.remove(request);
        Coord coord = requestLinks.remove(request).getCoord();
        pendingRequestsTree.remove(coord.getX(), coord.getY(), request);
    }

    static public class Factory implements AVDispatcherFactory {
        @Inject @Named(AVModule.AV_MODE)
        private Network network;

        @Inject private EventsManager eventsManager;

        @Inject @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject @Named(AVModule.AV_MODE)
        private ParallelLeastCostPathCalculator router;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig config) {
            return new SingleHeuristicDispatcher(
                    config.getParent().getId(),
                    eventsManager,
                    network,
                    new SingleRideAppender(config, router, travelTime)
            );
        }
    }
}
