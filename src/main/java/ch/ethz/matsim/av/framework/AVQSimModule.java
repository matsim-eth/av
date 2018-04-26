package ch.ethz.matsim.av.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.vehicles.VehicleType;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVOperatorConfig;
import ch.ethz.matsim.av.data.AVData;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.dispatcher.AVDispatchmentListener;
import ch.ethz.matsim.av.passenger.AVRequestCreator;
import ch.ethz.matsim.av.schedule.AVOptimizer;
import ch.ethz.matsim.av.vrpagent.AVActionCreator;
import ch.ethz.matsim.av.vrpagent.AVLegFactory;

public class AVQSimModule extends com.google.inject.AbstractModule {
    @Override
    protected void configure() {
        bind(AVOptimizer.class);
        bind(AVActionCreator.class);
        bind(AVRequestCreator.class);
        bind(AVDispatchmentListener.class);
    }
    
    @Provides @Singleton @Named("pickupDuration")
    public Double providePickupDuration(AVConfig config) {
    	return config.getTimingParameters().getPickupDurationPerStop();
    }

    @Provides @Singleton
    public PassengerEngine providePassengerEngine(EventsManager events, AVRequestCreator requestCreator, AVOptimizer optimizer, @Named(AVModule.AV_MODE) Network network) {
        return new PassengerEngine(
                AVModule.AV_MODE,
                events,
                requestCreator,
                optimizer,
                network
        );
    }

    @Provides
    @Singleton
    AVLegFactory provideLegFactory(AVOptimizer avOptimizer, QSim qsim) {
        return new AVLegFactory(qsim.getSimTimer(), avOptimizer);
    }

    @Provides @Singleton
    public VrpAgentSource provideAgentSource(AVActionCreator actionCreator, AVData data, AVOptimizer optimizer, @Named(AVModule.AV_MODE) VehicleType vehicleType, QSim qsim) {
        return new VrpAgentSource(actionCreator, data, optimizer, qsim, vehicleType);
    }

    @Provides @Singleton
    Map<Id<AVOperator>, AVDispatcher> provideDispatchers(Map<String, AVDispatcher.AVDispatcherFactory> factories, AVConfig config, Map<Id<AVOperator>, List<AVVehicle>> vehicles) {
        Map<Id<AVOperator>, AVDispatcher> dispatchers = new HashMap<>();

        for (AVOperatorConfig oc : config.getOperatorConfigs()) {
            AVDispatcherConfig dc = oc.getDispatcherConfig();
            String strategy = dc.getStrategyName();

            if (!factories.containsKey(strategy)) {
                throw new IllegalArgumentException("Dispatcher strategy '" + strategy + "' is not registered.");
            }

            AVDispatcher.AVDispatcherFactory factory = factories.get(strategy);
            AVDispatcher dispatcher = factory.createDispatcher(dc);

            for (AVVehicle vehicle : vehicles.get(oc.getId())) {
                dispatcher.addVehicle(vehicle);
                vehicle.setDispatcher(dispatcher);
            }

            dispatchers.put(oc.getId(), dispatcher);
        }

        return dispatchers;
    }
}
