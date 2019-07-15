package ch.ethz.matsim.av.passenger;

import java.util.Set;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.DynActivity;
import org.matsim.contrib.dynagent.DynAgent;

public class AVPassengerDropoffActivity implements DynActivity {
	private final PassengerEngine passengerEngine;
	private final DynAgent driver;
	private final Set<? extends PassengerRequest> requests;
	private final String activityType;
	private final StayTask dropoffTask;

	public AVPassengerDropoffActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle,
			StayTask dropoffTask, Set<? extends PassengerRequest> requests, String activityType) {
		this.activityType = activityType;
		this.dropoffTask = dropoffTask;

		this.passengerEngine = passengerEngine;
		this.driver = driver;
		this.requests = requests;

		if (requests.size() > vehicle.getCapacity()) {
			// Number of requests exceeds number of seats
			throw new IllegalStateException();
		}
	}

	@Override
	public void finalizeAction(double now) {
		for (PassengerRequest request : requests) {
			passengerEngine.dropOffPassenger(driver, request, now);
		}
	}

	@Override
	public String getActivityType() {
		return activityType;
	}

	@Override
	public double getEndTime() {
		return dropoffTask.getEndTime();
	}

	@Override
	public void doSimStep(double now) {

	}
}
