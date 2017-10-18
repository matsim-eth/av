package ch.ethz.matsim.av.electric.assets.station;

import java.util.Collection;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;

public interface Station extends StationLogic {
	Id<Station> getId();
	
	StationSpecification getSpecification();
	Link getLink();
	
	void simulate(double time);
	void reset();
}
