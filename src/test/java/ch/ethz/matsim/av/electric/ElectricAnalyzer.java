package ch.ethz.matsim.av.electric;

import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.core.controler.AbstractModule;

public class ElectricAnalyzer extends AbstractModule implements ActivityStartEventHandler {
    public long numberOfRechargingActivities = 0;

    @Override
    public void install() {
        addEventHandlerBinding().toInstance(this);
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getPersonId().toString().startsWith("av_") && event.getActType().equals("AVRecharge")) {
            numberOfRechargingActivities++;
        }
    }

    @Override
    public void reset(int iteration) {}
}
