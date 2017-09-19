package ch.ethz.matsim.av.framework;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.contrib.dynagent.run.DynActivityEngine;
import org.matsim.contrib.dynagent.run.DynActivityEnginePlugin;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimUtils;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVDispatchmentListener;
import ch.ethz.matsim.av.generator.AVVehicleCreator;
import ch.ethz.matsim.av.schedule.AVOptimizer;
import ch.ethz.matsim.av.vrpagent.AVActionCreator;

public class AVQSimProvider implements Provider<Mobsim> {
	@Inject
	private EventsManager eventsManager;
	@Inject
	private Collection<AbstractQSimPlugin> plugins;
	@Inject
	private Scenario scenario;

	@Inject
	private Injector injector;
	@Inject
	private AVConfig config;
	
	@Inject
	Config matsimConfig;
	
	@Inject
	Map<String, Class<? extends AVDispatcher.AVDispatcherFactory>> factoryTypes;
	
	@Override
	public Mobsim get() {
		/*
		 * This here is quite ugly. 
		 * We remove the default DynActivityEngine plugin in order to
		 * build our own instanceof DynActivityEngine which we can access.
		 * We need this access to call an initial handleActivity on AVs
		 * that are created online. Otherwise we do not get them into
		 * the loop.
		 */
		DynActivityEngine engine = new DynActivityEngine(eventsManager);
		
		Iterator<AbstractQSimPlugin> iterator = plugins.iterator();
		
		while (iterator.hasNext()) {
			AbstractQSimPlugin plugin = iterator.next();
			
			if (plugin instanceof DynActivityEnginePlugin) {
				iterator.remove();
			}
		}
		
		plugins.add(new AVDynActivityEnginePlugin(matsimConfig, engine));
		
		// End
		
		QSim qSim = QSimUtils.createQSim(scenario, eventsManager, plugins);
		Injector childInjector = injector.createChildInjector(new AVQSimModule(config, qSim, engine, factoryTypes));

		qSim.addQueueSimulationListeners(childInjector.getInstance(AVOptimizer.class));
		qSim.addQueueSimulationListeners(childInjector.getInstance(AVDispatchmentListener.class));

		qSim.addMobsimEngine(childInjector.getInstance(PassengerEngine.class));
		qSim.addDepartureHandler(childInjector.getInstance(PassengerEngine.class));
		qSim.addAgentSource(childInjector.getInstance(VrpAgentSource.class));

		return qSim;
	}
	
	private static class AVDynActivityEnginePlugin extends DynActivityEnginePlugin {
		final private DynActivityEngine engine;
		
		public AVDynActivityEnginePlugin(Config config, DynActivityEngine engine) {
			super(config);
			this.engine = engine;
		}
		
		public Collection<? extends Module> modules() {
			return Collections.singleton(new AbstractModule() {
				@Override
				protected void configure() {
					bind(DynActivityEngine.class).toInstance(engine);
				}
			});
		}
	}
}
