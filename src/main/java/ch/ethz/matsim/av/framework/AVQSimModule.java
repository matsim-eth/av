package ch.ethz.matsim.av.framework;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.ows.Request;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.contrib.dvrp.run.DvrpModeQSimModule;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.contrib.dvrp.vrpagent.VrpLeg;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.contrib.dynagent.run.DynActivityEngineModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.vehicles.VehicleType;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVOperatorConfig;
import ch.ethz.matsim.av.data.AVData;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVDispatchmentListener;
import ch.ethz.matsim.av.passenger.AVRequestCreator;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.matsim.av.schedule.AVOptimizer;
import ch.ethz.matsim.av.vrpagent.AVActionCreator;

public class AVQSimModule extends AbstractQSimModule {
	public final static String COMPONENT_NAME = "AVExtension";
	
	public static void configureComponents(QSimComponentsConfig components) {
		DynActivityEngineModule.configureComponents(components);
		components.addNamedComponent(COMPONENT_NAME);
		components.addNamedComponent(AVModule.AV_MODE);
	}
	
	@Override
	protected void configureQSim() {
		install(new DvrpModeQSimModule(AVModule.AV_MODE, true, Collections.emptySet()));
		
		bind(PassengerRequestCreator.class).annotatedWith(Names.named(AVModule.AV_MODE)).to(AVRequestCreator.class);
		bind(DynActionCreator.class).annotatedWith(Names.named(AVModule.AV_MODE)).to(AVActionCreator.class);
		bind(VrpOptimizer.class).annotatedWith(Names.named(AVModule.AV_MODE)).to(AVOptimizer.class);
		
		bind(AVOptimizer.class);
		bind(AVDispatchmentListener.class);
		
		addNamedComponent(AVOptimizer.class, COMPONENT_NAME);
		addNamedComponent(AVDispatchmentListener.class, COMPONENT_NAME);
	}

	@Provides
	@Singleton
	@Named("pickupDuration")
	public Double providePickupDuration(AVConfig config) {
		return config.getTimingParameters().getPickupDurationPerStop();
	}

	@Provides
	@Singleton
	VrpLegFactory provideLegCreator(AVOptimizer avOptimizer, QSim qsim) {
		return new VrpLegFactory() {
			@Override
			public VrpLeg create(Vehicle vehicle) {
				return VrpLegFactory.createWithOnlineTracker(TransportMode.car, vehicle, avOptimizer, qsim.getSimTimer());
			}
		};
	}

	@Provides
	@Singleton
	Map<Id<AVOperator>, AVDispatcher> provideDispatchers(Map<String, AVDispatcher.AVDispatcherFactory> factories,
			Map<Id<AVOperator>, AVRouter> routers, AVConfig config, Map<Id<AVOperator>, List<AVVehicle>> vehicles) {
		Map<Id<AVOperator>, AVDispatcher> dispatchers = new HashMap<>();

		for (AVOperatorConfig oc : config.getOperatorConfigs()) {
			AVDispatcherConfig dc = oc.getDispatcherConfig();
			String strategy = dc.getStrategyName();

			if (!factories.containsKey(strategy)) {
				throw new IllegalArgumentException("Dispatcher strategy '" + strategy + "' is not registered.");
			}

			AVRouter router = routers.get(oc.getId());

			AVDispatcher.AVDispatcherFactory factory = factories.get(strategy);
			AVDispatcher dispatcher = factory.createDispatcher(dc, router);

			for (AVVehicle vehicle : vehicles.get(oc.getId())) {
				dispatcher.addVehicle(vehicle);
				vehicle.setDispatcher(dispatcher);
			}

			dispatchers.put(oc.getId(), dispatcher);
		}

		return dispatchers;
	}
}
