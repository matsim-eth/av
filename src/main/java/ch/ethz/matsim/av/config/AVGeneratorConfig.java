package ch.ethz.matsim.av.config;

import org.matsim.core.config.ReflectiveConfigGroup;

public class AVGeneratorConfig extends ReflectiveConfigGroup {
    final static String GENERATOR = "generator";
    final static String NUMBER_OF_VEHICLES = "numberOfVehicles";
    final static String PREFIX = "prefix";
    final static String PATH_TO_SHP = "pathToSHP";

    final private AVOperatorConfig parent;

    final private String strategyName;

    private long numberOfVehicles = 10;
    private String prefix = null;
    private String pathToSHP = null;

    public AVGeneratorConfig(AVOperatorConfig parent, String strategyName) {
        super(GENERATOR, true);

        this.strategyName = strategyName;
        this.parent = parent;
    }

    public String getStrategyName() {
        return strategyName;
    }

    @StringSetter(NUMBER_OF_VEHICLES)
    public void setNumberOfVehicles(long numberOfVehicles) {
        this.numberOfVehicles = numberOfVehicles;
    }

    @StringGetter(NUMBER_OF_VEHICLES)
    public long getNumberOfVehicles() {
        return numberOfVehicles;
    }

    @StringSetter(PREFIX)
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @StringGetter(PREFIX)
    public String getPrefix() {
        return prefix;
    }

    @StringGetter(PATH_TO_SHP)
    public String getPathToSHP() {
        return pathToSHP;
    }

    @StringSetter(PATH_TO_SHP)
    public void setPathToSHP(String pathToSHP) {
        this.pathToSHP = pathToSHP;
    }

    public AVOperatorConfig getParent() {
        return parent;
    }
}
