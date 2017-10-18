package ch.ethz.matsim.av.electric.logic.tasks;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.AbstractTask;

import ch.ethz.matsim.av.electric.assets.station.Station;

public class RechargeTask extends AbstractTask implements TaskWithStation {
	final private Station station;
	
	public RechargeTask(double beginTime, double endTime, Station station) {
		super(beginTime, endTime);
		this.station = station;
	}
	
	public Station getStation() {
		return station;
	}

	@Override
	public Link getLink() {
		return station.getLink();
	}
}
