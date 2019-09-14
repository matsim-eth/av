package ch.ethz.matsim.av.cost;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.vehicles.Vehicle;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.generator.AVVehicleUtils;

public class FleetDistanceListener
		implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, LinkLeaveEventHandler {
	private final Map<Id<AVOperator>, Network> networks;
	private final Map<Id<Vehicle>, Integer> passengers = new HashMap<>();
	private final Map<Id<AVOperator>, OperatorData> data = new HashMap<>();

	class OperatorData {
		public int numberOfVehicles;
		public double vehicleDistance_m;
		public double passengerDistance_m;
	}

	public FleetDistanceListener(Set<Id<AVOperator>> operatorIds, Map<Id<AVOperator>, Network> networks) {
		this.networks = networks;

		for (Id<AVOperator> operatorId : operatorIds) {
			data.put(operatorId, new OperatorData());
		}
	}

	private void ensurePassengers(Id<Vehicle> vehicleId) {
		if (!passengers.containsKey(vehicleId)) {
			passengers.put(vehicleId, 0);
		}
	}

	private void increasePassengers(Id<Vehicle> vehicleId) {
		ensurePassengers(vehicleId);
		passengers.put(vehicleId, passengers.get(vehicleId) + 1);
	}

	private void decreasePassengers(Id<Vehicle> vehicleId) {
		ensurePassengers(vehicleId);
		int previous = passengers.put(vehicleId, passengers.get(vehicleId) - 1);

		if (previous == 0) {
			throw new IllegalStateException("Passenger count dropped below zero");
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("av:")) {
			if (event.getVehicleId().toString().startsWith("av:")) {
				Id<AVOperator> operatorId = AVVehicleUtils.getOperatorId(event.getVehicleId());

				if (data.containsKey(operatorId)) {
					increasePassengers(event.getVehicleId());
				}
			}
		}
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("av:")) {
			if (event.getVehicleId().toString().startsWith("av:")) {
				Id<AVOperator> operatorId = AVVehicleUtils.getOperatorId(event.getVehicleId());

				if (data.containsKey(operatorId)) {
					decreasePassengers(event.getVehicleId());
				}
			}
		}
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if (event.getVehicleId().toString().startsWith("av:")) {
			Id<AVOperator> operatorId = AVVehicleUtils.getOperatorId(event.getVehicleId());
			OperatorData operator = data.get(operatorId);

			if (operator != null) {
				ensurePassengers(event.getVehicleId());

				Network network = networks.get(operatorId);
				double linkLength = network.getLinks().get(event.getLinkId()).getLength();

				if (passengers.get(event.getVehicleId()) > 0) {
					operator.passengerDistance_m += linkLength;
				}

				operator.vehicleDistance_m += linkLength;
			}
		}
	}

	@Override
	public void reset(int iteration) {
		Set<Id<Vehicle>> vehicleIds = passengers.keySet();

		for (Id<Vehicle> vehicleId : vehicleIds) {
			passengers.put(vehicleId, 0);
		}

		Set<Id<AVOperator>> operatorIds = data.keySet();

		for (Id<AVOperator> operatorId : operatorIds) {
			data.put(operatorId, new OperatorData());
		}
	}
}
