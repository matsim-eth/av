package ch.ethz.matsim.av.electric.assets.station;

import java.util.Collection;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

@Singleton
public class EucledianStationFinder implements StationFinder {
	final private Collection<Station> stations;
	
	@Inject
	public EucledianStationFinder(Collection<Station> stations) {
		this.stations = stations;
	}
	
	@Override
	public Station findStationForVehicle(BatteryVehicle vehicle, Link link) {
		double minimumDistance = Double.MAX_VALUE;
		Station selectedStation = null;
		
		for (Station station : stations) {
			double distance = CoordUtils.calcEuclideanDistance(station.getLink().getCoord(), link.getCoord());
			
			if (distance < minimumDistance) {
				minimumDistance = distance;
				selectedStation = station;
			}
		}
		
		return selectedStation;
	}
}
