package ch.ethz.matsim.av.analysis.vehicles;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.vehicles.Vehicle;

import ch.ethz.matsim.av.analysis.LinkFinder;
import ch.ethz.matsim.av.analysis.PassengerTracker;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.generator.AVUtils;

public class VehicleAnalysisListener implements PersonDepartureEventHandler, PersonArrivalEventHandler,
		ActivityStartEventHandler, ActivityEndEventHandler, LinkLeaveEventHandler, PersonEntersVehicleEventHandler,
		PersonLeavesVehicleEventHandler {
	private final LinkFinder linkFinder;
	private final PassengerTracker passengers = new PassengerTracker();

	private final List<VehicleMovementItem> movements = new LinkedList<>();
	private final List<VehicleActivityItem> activities = new LinkedList<>();

	private final Map<Id<Vehicle>, VehicleMovementItem> currentMovements = new HashMap<>();
	private final Map<Id<Vehicle>, VehicleActivityItem> currentActivities = new HashMap<>();

	public VehicleAnalysisListener(LinkFinder linkFinder) {
		this.linkFinder = linkFinder;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getPersonId().toString().startsWith("av:")) {
			Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getPersonId());
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleMovementItem movement = new VehicleMovementItem();
			movements.add(movement);

			movement.operatorId = operatorId;
			movement.vehicleId = vehicleId;

			movement.originLink = linkFinder.getLink(event.getLinkId());
			movement.departureTime = event.getTime();

			currentMovements.put(vehicleId, movement);
		}
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if (event.getVehicleId().toString().startsWith("av:")) {
			VehicleMovementItem movement = currentMovements.remove(event.getVehicleId());

			if (movement == null) {
				throw new IllegalStateException("Found link enter event without departure");
			}

			movement.distance += linkFinder.getDistance(event.getLinkId());
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("av:")) {
			if (event.getVehicleId().toString().startsWith("av:")) {
				VehicleMovementItem movement = currentMovements.remove(event.getVehicleId());

				if (movement == null) {
					throw new IllegalStateException("Found person enter event without departure");
				}

				passengers.addPassenger(event.getVehicleId(), event.getPersonId());
				movement.numberOfPassengers = Math.max(passengers.getNumberOfPassengers(event.getVehicleId()),
						movement.numberOfPassengers);
			}
		}
	}

	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		if (!event.getPersonId().toString().startsWith("av:")) {
			if (event.getVehicleId().toString().startsWith("av:")) {
				VehicleMovementItem movement = currentMovements.remove(event.getVehicleId());

				if (movement == null) {
					throw new IllegalStateException("Found person leave event without departure");
				}

				passengers.removePassenger(event.getVehicleId(), event.getPersonId());
			}
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (event.getPersonId().toString().startsWith("av:")) {
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleMovementItem movement = currentMovements.remove(vehicleId);

			if (movement == null) {
				throw new IllegalStateException("Found arrival without departure");
			}

			movement.destinationLink = linkFinder.getLink(event.getLinkId());
			movement.arrivalTime = event.getTime();
		}
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		if (event.getPersonId().toString().startsWith("av:")) {
			Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getPersonId());
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleActivityItem activity = new VehicleActivityItem();
			activities.add(activity);

			activity.operatorId = operatorId;
			activity.vehicleId = vehicleId;

			activity.link = linkFinder.getLink(event.getLinkId());
			activity.type = event.getActType();

			activity.startTime = event.getTime();

			currentActivities.put(vehicleId, activity);
		}
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if (event.getPersonId().toString().startsWith("av:")) {
			Id<AVOperator> operatorId = AVUtils.getOperatorId(event.getPersonId());
			Id<Vehicle> vehicleId = Id.createVehicleId(event.getPersonId());

			VehicleActivityItem activity = currentActivities.remove(vehicleId);
			boolean isStarted = activity != null;

			if (!isStarted) {
				activity = new VehicleActivityItem();
				activities.add(activity);
			}

			activity.operatorId = operatorId;
			activity.vehicleId = vehicleId;

			activity.link = linkFinder.getLink(event.getLinkId());
			activity.type = event.getActType();

			activity.endTime = event.getTime();
		}
	}

	@Override
	public void reset(int iteration) {
		passengers.clear();

		currentActivities.clear();
		currentMovements.clear();

		activities.clear();
		movements.clear();
	}

	public List<VehicleActivityItem> getActivities() {
		return activities;
	}

	public List<VehicleMovementItem> getMovements() {
		return movements;
	}
}
