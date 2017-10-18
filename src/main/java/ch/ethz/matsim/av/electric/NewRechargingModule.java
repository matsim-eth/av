package ch.ethz.matsim.av.electric;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.electric.assets.battery.BatterySpecification;
import ch.ethz.matsim.av.electric.assets.battery.DefaultBatterySpecification;
import ch.ethz.matsim.av.electric.assets.station.EucledianStationFinder;
import ch.ethz.matsim.av.electric.assets.station.ParallelFIFO;
import ch.ethz.matsim.av.electric.assets.station.ParallelFIFOSpecification;
import ch.ethz.matsim.av.electric.assets.station.Station;
import ch.ethz.matsim.av.electric.assets.station.StationFinder;
import ch.ethz.matsim.av.electric.assets.station.StationResetter;
import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicleGenerator;
import ch.ethz.matsim.av.electric.consumption.ConsumptionCalculator;
import ch.ethz.matsim.av.electric.consumption.NullConsumptionCalculator;
import ch.ethz.matsim.av.electric.consumption.StaticConsumptionCalculator;
import ch.ethz.matsim.av.electric.logic.ChargeStateLogic;
import ch.ethz.matsim.av.electric.logic.ChargeStateLogicImpl;
import ch.ethz.matsim.av.electric.logic.MaximumEucledianDistanceRechargingController;
import ch.ethz.matsim.av.electric.logic.NewRechargingDispatcher;
import ch.ethz.matsim.av.electric.logic.RechargingController;
import ch.ethz.matsim.av.electric.logic.ThresholdRechargingController;
import ch.ethz.matsim.av.electric.logic.action.RechargeActionCreatorFactory;
import ch.ethz.matsim.av.electric.tracker.ConsumptionTracker;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVUtils;
import ch.ethz.matsim.av.vrpagent.AVActionCreatorFactory;
import ch.ethz.matsim.av.vrpagent.DefaultAVActionCreatorFactory;

public class NewRechargingModule extends AbstractModule {
	@Override
	public void install() {
		AVUtils.registerDispatcherFactory(binder(), "NewRecharging", NewRechargingDispatcher.Factory.class);
		AVUtils.registerGeneratorFactory(binder(), "Battery", BatteryVehicleGenerator.Factory.class);
		
		bind(ChargeStateLogic.class).to(ChargeStateLogicImpl.class);
		bind(StationFinder.class).to(EucledianStationFinder.class);
		bind(AVActionCreatorFactory.class).to(RechargeActionCreatorFactory.class);
		bind(ConsumptionCalculator.class).to(NullConsumptionCalculator.class);
		bind(RechargingController.class).to(MaximumEucledianDistanceRechargingController.class);
		addControlerListenerBinding().to(StationResetter.class);
	}
	
	@Provides @Singleton
	public MaximumEucledianDistanceRechargingController provideEucledianDistanceRechargingController(Collection<Station> stations) {
		return new MaximumEucledianDistanceRechargingController(stations);
	}
	
	@Provides @Singleton
	public StationResetter provideStationResetter(Collection<Station> stations) {
		return new StationResetter(stations);
	}
	
	@Provides @Singleton
	public RechargeActionCreatorFactory provideRechargeActionCreatorFactory(DefaultAVActionCreatorFactory delegate) {
		return new RechargeActionCreatorFactory(delegate);
	}
}
