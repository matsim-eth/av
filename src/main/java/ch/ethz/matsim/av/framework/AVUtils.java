package ch.ethz.matsim.av.framework;

import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.generator.AVGenerator;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;

public class AVUtils {
    static public LinkedBindingBuilder<Class<? extends AVDispatcher.AVDispatcherFactory>> bindDispatcherFactoryType(Binder binder, String dispatcherName) {
        MapBinder<String, Class<? extends AVDispatcher.AVDispatcherFactory>> dispatcherFactoryTypeBinder = MapBinder.newMapBinder(
                binder, new TypeLiteral<String>() {}, new TypeLiteral<Class<? extends AVDispatcher.AVDispatcherFactory>>() {});
        return dispatcherFactoryTypeBinder.addBinding(dispatcherName);
    }

    static public LinkedBindingBuilder<AVGenerator.AVGeneratorFactory> bindGeneratorFactory(Binder binder, String generatorName) {
        MapBinder<String, AVGenerator.AVGeneratorFactory> map = MapBinder.newMapBinder(
                binder, String.class, AVGenerator.AVGeneratorFactory.class);
        return map.addBinding(generatorName);
    }

    static public void registerDispatcherFactoryType(Binder binder, String dispatcherName, Class<? extends AVDispatcher.AVDispatcherFactory> factoryClass) {
    	bindDispatcherFactoryType(binder, dispatcherName).toInstance(factoryClass);
    }

    static public void registerGeneratorFactory(Binder binder, String dispatcherName, Class<? extends AVGenerator.AVGeneratorFactory> factoryClass) {
        bindGeneratorFactory(binder, dispatcherName).to(factoryClass);
    }
}
