package ch.ethz.matsim.av.electric.logic;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.matsim.contrib.dvrp.schedule.Task;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.electric.assets.battery.Battery;
import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;
import ch.ethz.matsim.av.electric.logic.tasks.TaskWithStation;
import ch.ethz.matsim.av.electric.tracker.ConsumptionTracker;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

@Singleton
public class ChargeStateLogicImpl implements ChargeStateLogic {
	final private Logger logger = Logger.getLogger(ChargeStateLogicImpl.class);
	final private Collection<Station> stations;
	final private ConsumptionTracker tracker;
	
	@Inject
	public ChargeStateLogicImpl(Collection<Station> stations, ConsumptionTracker tracker) {
		this.stations = stations;
		this.tracker = tracker;
	}

	@Override
	public void registerConsumptionForTask(BatteryVehicle vehicle, Task task) {
		Battery battery = vehicle.getBattery();
		
		double beforeState = battery.getState();
		double consumption = 0.0;
		
		if (task instanceof AVDriveTask) {
			double distanceBasedConsumption = vehicle.getConsumptionCalculator().calculateConsumptionForPath(task.getBeginTime(), task.getEndTime(), ((AVDriveTask) task).getPath());
			
			tracker.addDistanceBasedConsumption(task.getBeginTime(), task.getEndTime(), distanceBasedConsumption);
			consumption += distanceBasedConsumption;
		}
		
		if (!(task instanceof AVStayTask || task instanceof TaskWithStation || (task instanceof AVDriveTask && ((AVDriveTask) task).isUnoccupied()))) {
			double timeBasedConsumption = vehicle.getConsumptionCalculator().calculateConsumptionForDuration(task.getBeginTime(), task.getEndTime());
			tracker.addTimeBasedConsumption(task.getBeginTime(), task.getEndTime(), timeBasedConsumption);
			consumption += timeBasedConsumption;
		}
		
		battery.setState(beforeState - consumption);
		
		if (battery.getState() < 0.0 && beforeState >= 0.0) {
			logger.warn(String.format("Charge state of vehicle %s dropped below zero!", vehicle));
		}
	}

	@Override
	public void simulate(double now) {
		for (Station station : stations) {
			station.simulate(now);
		}
	}
}
