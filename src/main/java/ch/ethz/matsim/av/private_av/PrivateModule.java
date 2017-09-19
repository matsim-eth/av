package ch.ethz.matsim.av.private_av;

import org.matsim.core.controler.AbstractModule;

import ch.ethz.matsim.av.framework.AVUtils;

public class PrivateModule extends AbstractModule {
	@Override
	public void install() {
		AVUtils.registerDispatcherFactoryType(binder(), "private", PrivateDispatcher.Factory.class);
	}
}
