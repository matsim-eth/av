package ch.ethz.matsim.av.dispatcher.personal;

import org.matsim.core.controler.AbstractModule;

import ch.ethz.matsim.av.framework.AVUtils;

public class PersonalDispatcherModule extends AbstractModule {
	@Override
	public void install() {
		AVUtils.registerDispatcherFactoryType(binder(), "personal", PersonalDispatcher.Factory.class);
	}
}
