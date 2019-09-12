package ch.ethz.matsim.av.schedule;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.passenger.AVRequest;

public class AVTransitEvent extends GenericEvent implements HasPersonId {
	final private AVRequest request;

	final private Id<Person> personId;
	final private Id<AVOperator> operatorId;
	final private double distance;

	public AVTransitEvent(AVRequest request, double time) {
		this(request.getPassenger().getId(), request.getOperator().getId(), request.getRoute().getDistance(), time, request);
	}

	public AVTransitEvent(Id<Person> personId, Id<AVOperator> operatorId, double distance, double time) {
		this(personId, operatorId, distance, time, null);
	}

	private AVTransitEvent(Id<Person> personId, Id<AVOperator> operatorId, double distance, double time,
			AVRequest request) {
		super("AVTransit", time);

		this.request = request;
		this.personId = personId;
		this.operatorId = operatorId;
		this.distance = distance;
	}

	public AVRequest getRequest() {
		if (request == null) {
			throw new IllegalStateException();
		}

		return request;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put("person", personId.toString());
		attr.put("operator", operatorId.toString());
		attr.put("distance", String.valueOf(distance));
		return attr;
	}

	@Override
	public Id<Person> getPersonId() {
		return personId;
	}

	public double getDistance() {
		return distance;
	}

	public Id<AVOperator> getOperatorId() {
		return operatorId;
	}
}
