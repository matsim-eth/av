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
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Singleton
/**
 * TODO: This whole class should be revised. There have been several iterations
 * for changing it and there are some redundant left-overs. /shoerl, dec 2017
 *
 */
public class AVOptimizer implements VrpOptimizerWithOnlineTracking, MobsimBeforeSimStepListener {
    private double now;

    @Inject private Map<Id<AVOperator>, AVDispatcher> dispatchers;
    @Inject private EventsManager eventsManager;

    @Override
    public void requestSubmitted(Request request) {
    	AVRequest avRequest = (AVRequest) request;
    	AVDispatcher dispatcher = avRequest.getDispatcher();
    	
    	synchronized (dispatcher) {
    		dispatcher.onRequestSubmitted(avRequest);
		}
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
            nextTask = (AVTask)tasks.get(index);
        } else {
        	throw new IllegalStateException("An AV schedule should never end!");
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

        ensureNonFinishingSchedule(schedule);
        schedule.nextTask();
        ensureNonFinishingSchedule(schedule);
        
        AVDispatcher dispatcher = ((AVVehicle) vehicle).getDispatcher();

        if (nextTask != null) {
        	synchronized(dispatcher) {
        		dispatcher.onNextTaskStarted((AVVehicle) vehicle);
        	}
        }

        if (nextTask != null && nextTask instanceof AVDropoffTask) {
            processTransitEvent((AVDropoffTask) nextTask);
        }
    }
    
    private void ensureNonFinishingSchedule(Schedule schedule) {
    	AVTask lastTask = (AVTask) Schedules.getLastTask(schedule);
    	
    	if (lastTask.getAVTaskType() != AVTask.AVTaskType.STAY) {
    		throw new IllegalStateException("An AV schedule should always end with a STAY task");
    	}
    	
    	if (!Double.isInfinite(lastTask.getEndTime())) {
    		throw new IllegalStateException("An AV schedule should always end at time Infinity");
    	}
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
        now = e.getSimulationTime();
    }

    private void processTransitEvent(AVDropoffTask task) {
        for (AVRequest request : task.getRequests()) {
            eventsManager.processEvent(new AVTransitEvent(request, now));
        }
    }

    @Override
    public void vehicleEnteredNextLink(Vehicle vehicle, Link link) {}
}
