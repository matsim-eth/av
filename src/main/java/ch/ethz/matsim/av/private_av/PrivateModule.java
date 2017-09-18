package ch.ethz.matsim.av.private_av;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.controler.AbstractModule;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVUtils;

public class PrivateModule extends AbstractModule {
	@Override
	public void install() {
		AVUtils.registerGeneratorFactory(binder(), "private", PrivateGenerator.Factory.class);
		AVUtils.registerDispatcherFactory(binder(), "private", PrivateDispatcher.Factory.class);
	}
	
	@Singleton @Provides
	public PrivateScheduleRepository providePrivateScheduleRepository(Population population, @Named(AVModule.AV_MODE) Network network) {
		return new PrivateScheduleRepository(population, network);
	}
}
