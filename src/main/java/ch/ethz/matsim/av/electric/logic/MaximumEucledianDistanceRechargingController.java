package ch.ethz.matsim.av.electric.logic;

import java.util.Collection;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.station.StationFinder;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public class MaximumEucledianDistanceRechargingController implements RechargingController {
	final private Collection<Station> stations;

	public MaximumEucledianDistanceRechargingController(Collection<Station> stations) {
		this.stations = stations;
	}

	@Override
	public boolean shouldRecharge(BatteryVehicle vehicle) {
		double time = vehicle.getSchedule().getCurrentTask().getBeginTime();
		Link location = ((StayTask) vehicle.getSchedule().getCurrentTask()).getLink();
		
		double maximumDistance = 0.0;
		
		for (Station station : stations) {
			maximumDistance = Math.max(maximumDistance, CoordUtils.calcEuclideanDistance(station.getLink().getCoord(), location.getCoord()));
		}
		
		double minimumChargeState = 2.0 * vehicle.getConsumptionCalculator().calculateConsumptionForDistance(time, time, maximumDistance);
		return vehicle.getBattery().getState() < minimumChargeState;
	}

	@Override
	public void simulate(double now) {}
}
