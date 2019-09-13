package ch.ethz.matsim.av.config.operator;

import org.matsim.api.core.v01.Id;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.data.AVOperator;

public class OperatorConfig extends ReflectiveConfigGroup {
	static public final String GROUP_NAME = "operator";

	static public final String ID = "id";
	static public final String ROUTER_TYPE = "routerType";

	static public final Id<AVOperator> DEFAULT_OPERATOR_ID = AVOperator.createId("default");
	private Id<AVOperator> id = DEFAULT_OPERATOR_ID;

	private final DispatcherConfig dispatcherConfig = new DispatcherConfig();
	private final GeneratorConfig generatorConfig = new GeneratorConfig();
	private final TimingConfig timingConfig = new TimingConfig();
	private final PricingConfig pricingConfig = new PricingConfig();
	private final RouterConfig routerConfig = new RouterConfig();

	public OperatorConfig() {
		super(GROUP_NAME);
		
		addParameterSet(dispatcherConfig);
		addParameterSet(generatorConfig);
		addParameterSet(timingConfig);
		addParameterSet(pricingConfig);
		addParameterSet(routerConfig);
	}

	@StringGetter(ID)
	public String getIdAsString() {
		return id.toString();
	}

	@StringSetter(ID)
	public void setIdAsString(String id) {
		this.id = AVOperator.createId(id);
	}

	public Id<AVOperator> getId() {
		return id;
	}

	public void setId(Id<AVOperator> id) {
		this.id = id;
	}

	public DispatcherConfig getDispatcherConfig() {
		return dispatcherConfig;
	}

	public GeneratorConfig getGeneratorConfig() {
		return generatorConfig;
	}

	public TimingConfig getTimingConfig() {
		return timingConfig;
	}

	public PricingConfig getPricingConfig() {
		return pricingConfig;
	}

	public RouterConfig getRouterConfig() {
		return routerConfig;
	}

	@Override
	public ConfigGroup createParameterSet(final String type) {
		switch (type) {
		case DispatcherConfig.GROUP_NAME:
			return dispatcherConfig;
		case GeneratorConfig.GROUP_NAME:
			return generatorConfig;
		case TimingConfig.GROUP_NAME:
			return timingConfig;
		case PricingConfig.GROUP_NAME:
			return pricingConfig;
		case RouterConfig.GROUP_NAME:
			return routerConfig;
		}

		throw new IllegalStateException("Unknown parameter set for operator: " + type);
	}
}
