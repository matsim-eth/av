package ch.ethz.matsim.av.framework;

import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.router.AVRouter;

import com.google.inject.Binder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

public class AVUtils {
    static public LinkedBindingBuilder<AVDispatcher.AVDispatcherFactory> bindDispatcherFactory(Binder binder, String dispatcherName) {
        MapBinder<String, AVDispatcher.AVDispatcherFactory> map = MapBinder.newMapBinder(
                binder, String.class, AVDispatcher.AVDispatcherFactory.class);
        return map.addBinding(dispatcherName);
    }

    static public LinkedBindingBuilder<AVGenerator.AVGeneratorFactory> bindGeneratorFactory(Binder binder, String generatorName) {
        MapBinder<String, AVGenerator.AVGeneratorFactory> map = MapBinder.newMapBinder(
                binder, String.class, AVGenerator.AVGeneratorFactory.class);
        return map.addBinding(generatorName);
    }
    
    static public LinkedBindingBuilder<AVRouter.Factory> bindRouterFactory(Binder binder, String routerName) {
        MapBinder<String, AVRouter.Factory> map = MapBinder.newMapBinder(
                binder, String.class, AVRouter.Factory.class);
        return map.addBinding(routerName);
    }

    static public void registerDispatcherFactory(Binder binder, String dispatcherName, Class<? extends AVDispatcher.AVDispatcherFactory> factoryClass) {
        bindDispatcherFactory(binder, dispatcherName).to(factoryClass);
    }

    static public void registerGeneratorFactory(Binder binder, String dispatcherName, Class<? extends AVGenerator.AVGeneratorFactory> factoryClass) {
        bindGeneratorFactory(binder, dispatcherName).to(factoryClass);
    }
    
    static public void registerRouterFactory(Binder binder, String routerName, Class<? extends AVRouter.Factory> factoryClass) {
    	bindRouterFactory(binder, routerName).to(factoryClass);
    }
}
