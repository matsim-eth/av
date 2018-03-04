package ch.ethz.matsim.av.framework;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;

import com.google.inject.Module;

import ch.ethz.matsim.av.dispatcher.AVDispatchmentListener;
import ch.ethz.matsim.av.schedule.AVOptimizer;

public class AVQSimPlugin extends AbstractQSimPlugin {
	public AVQSimPlugin(Config config) {
		super(config);
	}

	public Collection<? extends Module> modules() {
		return Collections.singleton(new AVQSimModule());
	}
	
	public Collection<Class<? extends MobsimEngine>> engines() {
		return Collections.singleton(PassengerEngine.class);
	}

	public Collection<Class<? extends MobsimListener>> listeners() {
		return Arrays.asList(AVOptimizer.class, AVDispatchmentListener.class);
	}

	public Collection<Class<? extends AgentSource>> agentSources() {
		return Collections.singleton(VrpAgentSource.class);
	}

	public Collection<Class<? extends DepartureHandler>> departureHandlers() {
		return Collections.singleton(PassengerEngine.class);
	}
}
