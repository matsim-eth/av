package ch.ethz.matsim.av.data;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.data.Vehicle;

import com.google.common.collect.ImmutableMap;

public class AVData implements Fleet {
	private final Map<Id<Vehicle>, AVVehicle> vehicles;

	public AVData(Map<Id<Vehicle>, AVVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	@Override
	public ImmutableMap<Id<Vehicle>, ? extends Vehicle> getVehicles() {
		return ImmutableMap.copyOf(vehicles);
	}

	@Override
	public void resetSchedules() {

	}
}