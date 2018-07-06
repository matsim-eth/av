package ch.ethz.matsim.av.config;

import ch.ethz.matsim.av.data.AVOperator;
import org.matsim.api.core.v01.Id;
import org.matsim.core.config.ReflectiveConfigGroup;

public class AVOperatorConfig extends ReflectiveConfigGroup {
    final static String OPERATOR = "operator";

    final private AVConfig parentConfig;
    final private Id<AVOperator> id;

    private AVTimingParameters timingParameters = null;
    private AVPriceStructureConfig priceStructureConfig = null;
    private AVDispatcherConfig dispatcherConfig = null;
    private AVGeneratorConfig generatorConfig = null;
    private String routerName = "DefaultAVRouter";

    public AVOperatorConfig(String id, AVConfig parentConfig) {
        super(OPERATOR);
        this.parentConfig = parentConfig;
        this.id = Id.create(id, AVOperator.class);
    }

    public Id<AVOperator> getId() {
        return id;
    }

    public AVTimingParameters getTimingParameters() {
        if (timingParameters == null) {
            return parentConfig.getTimingParameters();
        }

        return timingParameters;
    }

    public AVTimingParameters createTimingParameters() {
        timingParameters = new AVTimingParameters();
        return timingParameters;
    }

    public AVDispatcherConfig getDispatcherConfig() {
        return dispatcherConfig;
    }

    public AVDispatcherConfig createDispatcherConfig(String name) {
        dispatcherConfig = new AVDispatcherConfig(this, name);
        return dispatcherConfig;
    }

    public AVGeneratorConfig getGeneratorConfig() {
        return generatorConfig;
    }

    public AVGeneratorConfig createGeneratorConfig(String name) {
        generatorConfig = new AVGeneratorConfig(this, name);
        return generatorConfig;
    }

    public AVPriceStructureConfig getPriceStructureConfig() {
        return priceStructureConfig;
    }

    public AVPriceStructureConfig createPriceStructureConfig() {
        priceStructureConfig = new AVPriceStructureConfig();
        return priceStructureConfig;
    }
    
    @StringGetter("routerName")
    public String getRouterName() {
    	return routerName;
    }
    
    @StringSetter("routerName")
    public void setRouterName(String routerName) {
    	this.routerName = routerName;
    }
}
