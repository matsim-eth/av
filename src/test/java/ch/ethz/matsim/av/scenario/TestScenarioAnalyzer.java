package ch.ethz.matsim.av.scenario;

import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.core.controler.AbstractModule;

import ch.ethz.matsim.av.routing.AVRoutingModule;

public class TestScenarioAnalyzer extends AbstractModule
		implements PersonDepartureEventHandler, PersonArrivalEventHandler, ActivityStartEventHandler {
	public long numberOfDepartures;
	public long numberOfArrivals;
	public long numberOfInteractionActivities;

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
	public void reset(int iteration) {
	}

	@Override
	public void install() {
		addEventHandlerBinding().toInstance(this);
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		if (event.getActType().equals(AVRoutingModule.INTERACTION_ACTIVITY_TYPE)) {
			numberOfInteractionActivities++;
		}
	}
}
