package ch.ethz.matsim.av.electric.logic.tasks;

import org.matsim.contrib.dvrp.schedule.StayTask;

import ch.ethz.matsim.av.electric.assets.station.Station;

public interface TaskWithStation extends StayTask {
	Station getStation();
}
