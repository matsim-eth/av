package ch.ethz.matsim.av.dispatcher.scheduled;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule.TripScheduler;
import ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule.TripSchedulerFactory;
import ch.ethz.matsim.av.framework.AVUtils;

public class ScheduledDispatcherModule extends AbstractModule {
	final private static String DISPATCHER_NAME = "scheduled";
	
	@Override
	public void install() {
		AVUtils.registerDispatcherFactoryType(binder(), DISPATCHER_NAME, ScheduledDispatcher.Factory.class);
		
		MapBinder<String, TripSchedulerFactory> map = MapBinder.newMapBinder(binder(), String.class, TripSchedulerFactory.class);
		map.addBinding("personal").to(PersonalScheduler.Factory.class);
	}
	
	@Provides @Singleton
	public Map<Id<AVOperator>, TripScheduler> provideTripScheduleRepositories(Map<Id<AVOperator>, AVOperator> operators, Map<String, TripSchedulerFactory> factories) {
		Map<Id<AVOperator>, TripScheduler> schedulers = new HashMap<>();
		
		for (AVOperator operator : operators.values()) {
			AVDispatcherConfig dispatcherConfig = operator.getConfig().getDispatcherConfig();
			
			if (dispatcherConfig.getStrategyName().equals(DISPATCHER_NAME)) {
				String schedulerId = dispatcherConfig.getParams().getOrDefault("scheduler", null);
				TripSchedulerFactory factory = factories.get(schedulerId);
				
				if (factory == null) {
					throw new IllegalStateException("Scheduler '" + schedulerId +  "' not found for operator '" + operator.getId().toString() + "'");
				}
				
				schedulers.put(operator.getId(), factory.createScheduler(operator.getId()));
			}
		}
		
		return schedulers;
	}
}
