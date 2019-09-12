package ch.ethz.matsim.av.dispatcher;

import ch.ethz.matsim.av.data.AVOperator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.utils.misc.Time;

import java.util.Map;

@Singleton
public class AVDispatchmentListener implements MobsimBeforeSimStepListener {
    @Inject
    Map<Id<AVOperator>, AVDispatcher> dispatchers;

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
        for (AVDispatcher dispatcher : dispatchers.values()) {
            dispatcher.onNextTimestep(e.getSimulationTime());
        }
    }
}
