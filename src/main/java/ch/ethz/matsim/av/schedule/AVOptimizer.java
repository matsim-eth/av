package ch.ethz.matsim.av.schedule;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.passenger.AVRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizerWithOnlineTracking;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Singleton
public class AVOptimizer implements VrpOptimizerWithOnlineTracking, MobsimBeforeSimStepListener {
    private double now;
    final private List<AVRequest> submittedRequestsBuffer = Collections.synchronizedList(new LinkedList<>());

    @Inject private Map<Id<AVOperator>, AVDispatcher> dispatchers;
    @Inject private EventsManager eventsManager;

    @Override
    public void requestSubmitted(Request request) {
        // TODO: IS this necessary?
        submittedRequestsBuffer.add((AVRequest) request);
    }

    private void processSubmittedRequestsBuffer() {
        for (AVRequest request : submittedRequestsBuffer) {
            request.getDispatcher().onRequestSubmitted(request);
        }

        submittedRequestsBuffer.clear();
    }

    @Override
    public void nextTask(Vehicle vehicle) {
        Schedule schedule = vehicle.getSchedule();
        if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED) {
            schedule.nextTask();
            return;
        }

        Task currentTask = schedule.getCurrentTask();
        currentTask.setEndTime(now);

        List<? extends Task> tasks = schedule.getTasks();
        int index = currentTask.getTaskIdx() + 1;
        Task nextTask = null;

        if (index < tasks.size()) {
            nextTask = tasks.get(index);
        }

        double startTime = now;

        Task indexTask;
        while (index < tasks.size()) {
            indexTask = tasks.get(index);

            if (indexTask instanceof StayTask) {
                if (indexTask.getEndTime() < startTime) indexTask.setEndTime(startTime);
            } else {
                indexTask.setEndTime(indexTask.getEndTime() - indexTask.getBeginTime() + startTime);
            }

            indexTask.setBeginTime(startTime);
            startTime = indexTask.getEndTime();
            index++;
        }

        schedule.nextTask();

        if (nextTask != null) {
            ((AVVehicle) vehicle).getDispatcher().onNextTaskStarted((AVVehicle) vehicle);
        }

        if (nextTask != null && nextTask instanceof AVDropoffTask) {
            processTransitEvent((AVDropoffTask) nextTask);
        }
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
        now = e.getSimulationTime();
        processSubmittedRequestsBuffer();
    }

    private void processTransitEvent(AVDropoffTask task) {
        for (AVRequest request : task.getRequests()) {
            eventsManager.processEvent(new AVTransitEvent(request, now));
        }
    }

    @Override
    public void vehicleEnteredNextLink(Vehicle vehicle, Link link) {}
}
