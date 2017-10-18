package ch.ethz.matsim.av.electric.logic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelDataImpl;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.station.StationFinder;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;
import ch.ethz.matsim.av.electric.logic.tasks.RechargeTask;
import ch.ethz.matsim.av.electric.logic.tasks.WaitForRechargeTask;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.LeastCostPathFuture;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.schedule.AVTask;

public class NewRechargingDispatcher implements AVDispatcher {
	final private AVDispatcher delegate;
	
	final private ChargeStateLogic chargeStateLogic;
	final private StationFinder stationFinder;
	final private RechargingController controller;
	
	final private ParallelLeastCostPathCalculator router;
	final private TravelTime travelTime;
	
	public NewRechargingDispatcher(AVDispatcher delegate, ChargeStateLogic chargeStateLogic, StationFinder stationFinder, RechargingController controller, ParallelLeastCostPathCalculator router, TravelTime travelTime) {
		this.delegate = delegate;
		this.chargeStateLogic = chargeStateLogic;
		this.travelTime = travelTime;
		this.router = router;
		this.stationFinder = stationFinder;
		this.controller = controller;
	}
	
	@Override
	public void onRequestSubmitted(AVRequest request) {
		delegate.onRequestSubmitted(request);		
	}

	@Override
	public void onNextTaskStarted(AVVehicle vehicle) {
		if (vehicle instanceof BatteryVehicle) {
			chargeStateLogic.registerConsumptionForTask((BatteryVehicle) vehicle, vehicle.getSchedule().getTasks().get(vehicle.getSchedule().getCurrentTask().getTaskIdx() - 1));
			
			Task task = vehicle.getSchedule().getCurrentTask();
			
			if (task instanceof AVStayTask) {
				if (controller.shouldRecharge((BatteryVehicle) vehicle)) {
					sendVehicleToRecharge((BatteryVehicle) vehicle, (AVStayTask) task);
					return;
				}
			}
			
			if (task instanceof WaitForRechargeTask) {
				((WaitForRechargeTask) task).getStation().notifyArrival((BatteryVehicle) vehicle, task.getBeginTime());
			}
			
			if (task instanceof RechargeTask) {
				((RechargeTask) task).getStation().notifyStartRecharge((BatteryVehicle) vehicle, task.getBeginTime());
			}
		}
		
		if (vehicle.getSchedule().getCurrentTask() instanceof AVTask) {
			delegate.onNextTaskStarted(vehicle);
		}
	}
	
	private class WaitingForRoute {
		final BatteryVehicle vehicle;
		final Station station;
		final AVStayTask task;
		final LeastCostPathFuture pathFuture;
		
		public WaitingForRoute(BatteryVehicle vehicle, Station station, AVStayTask task, LeastCostPathFuture pathFuture) {
			this.vehicle = vehicle;
			this.station = station;
			this.task = task;
			this.pathFuture = pathFuture;
		}
	}
	
	final private Set<WaitingForRoute> waitingForRoute = new HashSet<>();
	
	private void sendVehicleToRecharge(BatteryVehicle vehicle, AVStayTask task) {
		Station station = stationFinder.findStationForVehicle(vehicle, task.getLink());
		station.notifyRegistration(vehicle, task.getBeginTime());
		
		if (station.getLink().equals(task.getLink())) {		
			waitingForRoute.add(new WaitingForRoute(vehicle, station, task, null));
		} else {
			LeastCostPathFuture pathFuture = router.calcLeastCostPath(task.getLink().getToNode(), station.getLink().getFromNode(), task.getBeginTime(), null, null);
			waitingForRoute.add(new WaitingForRoute(vehicle, station, task, pathFuture));
		}
	}
	
	private void sendVehicleToRecharge(WaitingForRoute item, double now) {
		Schedule schedule = item.vehicle.getSchedule();
		
		Task lastTask = schedule.getCurrentTask();

		if(!schedule.getCurrentTask().equals(item.task)) {
			throw new IllegalStateException();
		}
		
		double scheduleEndTime = item.task.getEndTime();
		item.task.setEndTime(now);
		
		double waitStartTime = now;
		
		if (item.pathFuture != null) {
			LeastCostPathCalculator.Path path = item.pathFuture.get();
			VrpPathWithTravelData vrpPath = VrpPaths.createPath(item.task.getLink(), item.station.getLink(), now, path, travelTime);
					
			AVDriveTask driveTask = new AVDriveTask(vrpPath);
			schedule.addTask(driveTask);
			
			waitStartTime = driveTask.getEndTime();
		}
		
		//waitStartTime = Math.min(waitStartTime, scheduleEndTime);
		
		WaitForRechargeTask waitForRechargeTask = new WaitForRechargeTask(waitStartTime, waitStartTime, item.station);
		schedule.addTask(waitForRechargeTask);
		
		RechargeTask rechargeTask = new RechargeTask(waitStartTime, waitStartTime, item.station);
		schedule.addTask(rechargeTask);		
		
		AVStayTask endTask = new AVStayTask(waitStartTime, Math.max(waitStartTime, scheduleEndTime), item.station.getLink());
		schedule.addTask(endTask);
	}

	@Override
	public void onNextTimestep(double now) {
		Iterator<WaitingForRoute> iterator = waitingForRoute.iterator();
		
		while (iterator.hasNext()) {
			WaitingForRoute item = iterator.next();
			
			if (item.pathFuture == null || item.pathFuture.isDone()) {
				sendVehicleToRecharge(item, now);
				iterator.remove();
			}
		}
		
		delegate.onNextTimestep(now);
		chargeStateLogic.simulate(now);
		controller.simulate(now);
	}

	@Override
	public void addVehicle(AVVehicle vehicle) {
		delegate.addVehicle(vehicle);
	}

	@Override
	public void removeVehicle(AVVehicle vehicle) {
		delegate.removeVehicle(vehicle);
	}

	@Override
	public boolean hasVehicle(AVVehicle vehicle) {
		return delegate.hasVehicle(vehicle);
	}

	static public class Factory implements AVDispatcher.AVDispatcherFactory {
		@Inject Map<String, AVDispatcher.AVDispatcherFactory> dispatcherFactories;
		
		@Inject @Named(AVModule.AV_MODE) ParallelLeastCostPathCalculator router;
		@Inject @Named(AVModule.AV_MODE) TravelTime travelTime;
		
		@Inject ChargeStateLogic chargeStateLogic;
		@Inject StationFinder stationFinder;
		@Inject RechargingController rechargingController;
		
		@Override
		public AVDispatcher createDispatcher(AVDispatcherConfig config) {
            if (!config.getParams().containsKey("delegate")) {
                throw new IllegalArgumentException();
            }
            
            AVDispatcher delegate = dispatcherFactories.get(config.getParams().get("delegate")).createDispatcher(config);
            return new NewRechargingDispatcher(delegate, chargeStateLogic, stationFinder, rechargingController, router, travelTime);
		}
		
	}
}
