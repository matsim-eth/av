package ch.ethz.matsim.av.schedule;

import ch.ethz.matsim.av.passenger.AVRequest;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

import java.util.Map;

public class AVTransitEvent extends Event implements HasPersonId {
    final private AVRequest request;

    public AVTransitEvent(AVRequest request, double time) {
        super(time);
        this.request = request;
    }

    public AVRequest getRequest() {
        return request;
    }

    @Override
    public String getEventType() {
        return "AVTransit";
    }

    public Map<String, String> getAttributes() {
        Map<String, String> attr = super.getAttributes();
        attr.put("person", request.getPassenger().getId().toString());
        attr.put("operator", request.getOperator().getId().toString());
        attr.put("distance", String.valueOf(request.getRoute().getDistance()));
        return attr;
    }

    @Override
    public Id<Person> getPersonId() {
        return request.getPassenger().getId();
    }
}
