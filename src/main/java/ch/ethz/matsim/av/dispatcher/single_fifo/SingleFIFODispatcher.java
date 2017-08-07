package ch.ethz.matsim.av.dispatcher.single_fifo;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVVehicleAssignmentEvent;
import ch.ethz.matsim.av.dispatcher.utils.SingleRideAppender;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.schedule.AVTask;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.router.util.TravelTime;

import java.util.LinkedList;
import java.util.Queue;

public class SingleFIFODispatcher implements AVDispatcher {
    final private SingleRideAppender appender;
    final private Queue<AVVehicle> availableVehicles = new LinkedList<>();
    final private Queue<AVRequest> pendingRequests = new LinkedList<>();

    final private EventsManager eventsManager;

    private boolean reoptimize = false;

    public SingleFIFODispatcher(EventsManager eventsManager, SingleRideAppender appender) {
        this.appender = appender;
        this.eventsManager = eventsManager;
    }

    @Override
    public void onRequestSubmitted(AVRequest request) {
        pendingRequests.add(request);
        reoptimize = true;
    }

    @Override
    public void onNextTaskStarted(AVVehicle vehicle) {
    	AVTask task = (AVTask)vehicle.getSchedule().getCurrentTask();
        if (task.getAVTaskType() == AVTask.AVTaskType.STAY) {
            availableVehicles.add(vehicle);
        }
    }

    @Override
    public void addVehicle(AVVehicle vehicle) {
        availableVehicles.add(vehicle);
        eventsManager.processEvent(new AVVehicleAssignmentEvent(vehicle, 0));
    }

    @Override
    public void removeVehicle(AVVehicle vehicle) {
        availableVehicles.remove(vehicle);
    }

    @Override
    public boolean hasVehicle(AVVehicle vehicle) {
        return availableVehicles.contains(vehicle);
    }

    private void reoptimize(double now) {
        while (availableVehicles.size() > 0 && pendingRequests.size() > 0) {
            AVVehicle vehicle = availableVehicles.poll();
            AVRequest request = pendingRequests.poll();
            appender.schedule(request, vehicle, now);
        }

        reoptimize = false;
    }

    @Override
    public void onNextTimestep(double now) {
        appender.update();
        if (reoptimize) reoptimize(now);
    }

    static public class Factory implements AVDispatcherFactory {
        @Inject @Named(AVModule.AV_MODE)
        private ParallelLeastCostPathCalculator router;

        @Inject @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig config) {
            return new SingleFIFODispatcher(eventsManager, new SingleRideAppender(config, router, travelTime));
        }
    }
}
