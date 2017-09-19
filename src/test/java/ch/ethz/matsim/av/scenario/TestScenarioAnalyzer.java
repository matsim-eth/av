package ch.ethz.matsim.av.scenario;

import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

public class TestScenarioAnalyzer extends AbstractModule implements PersonDepartureEventHandler, PersonArrivalEventHandler {
    public long numberOfDepartures;
    public long numberOfArrivals;

    @Override
    public void handleEvent(PersonArrivalEvent event) {
    	if (event.getLegMode().equals("av")) {
    		numberOfArrivals++;
    	}
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
    	if (event.getLegMode().equals("av")) {
    		numberOfDepartures++;
    	}
    }

    @Override
    public void reset(int iteration) {}

    @Override
    public void install() {
        addEventHandlerBinding().toInstance(this);
    }
}
