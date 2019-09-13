package ch.ethz.matsim.av.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class AVConfigGroup extends ReflectiveConfigGroup {
	static final public String GROUP_NAME = "av";

	static final public String NUMBER_OF_PARALLEL_ROUTERS = "numberOfParallelRouters";

	static final public String ACCESS_EGRESS_TYPE = "accessEgressType";
	static final public String ACCESS_EGRESS_LINK_FLAG = "accessEgressLinkFlag";

	public enum AccessEgressType {
		NONE, MODE, ATTRIBUTE
	}

	private long parallelRouters = 4;

	private AccessEgressType accessEgressType = AccessEgressType.NONE;
	private String accessEgressLinkFlag = "avAccessEgress";

	public AVConfigGroup() {
		super(GROUP_NAME);
	}

	@Override
	public ConfigGroup createParameterSet(final String type) {
		switch (type) {
		case AVScoringParameterSet.GROUP_NAME:
			return new AVScoringParameterSet();
		case OperatorConfig.GROUP_NAME:
			return new OperatorConfig();
		}

		throw new IllegalStateException("Unknown parameter set in AV config: " + type);
	}

	public Map<Id<AVOperator>, OperatorConfig> getOperators() {
		Map<Id<AVOperator>, OperatorConfig> map = new HashMap<>();

		for (ConfigGroup _operator : getParameterSets(OperatorConfig.GROUP_NAME)) {
			OperatorConfig operator = (OperatorConfig) _operator;

			if (map.containsKey(operator.getId())) {
				throw new IllegalStateException("Error duplicate operator in config: " + operator.getId());
			}

			map.put(operator.getId(), operator);
		}

		return Collections.unmodifiableMap(map);
	}

	public void addOperator(OperatorConfig operator) {
		if (getOperators().containsKey(operator.getId())) {
			throw new IllegalStateException("Another operator with this ID exists already: " + operator.getId());
		}

		addParameterSet(operator);
	}

	public void removeOperator(Id<AVOperator> id) {
		OperatorConfig operator = getOperatorConfig(id);
		removeParameterSet(operator);
	}

	public OperatorConfig getOperatorConfig(Id<AVOperator> id) {
		OperatorConfig operator = getOperators().get(id);

		if (operator == null) {
			throw new IllegalStateException("Operator does not exist: " + id);
		}

		return operator;
	}

	public void clearOperators() {
		clearParameterSetsForType(OperatorConfig.GROUP_NAME);
	}

	public Map<String, AVScoringParameterSet> getScoringParameters() {
		Map<String, AVScoringParameterSet> map = new HashMap<>();

		for (ConfigGroup _parameters : getParameterSets(AVScoringParameterSet.GROUP_NAME)) {
			AVScoringParameterSet parameters = (AVScoringParameterSet) _parameters;

			if (map.containsKey(parameters.getSubpopulation())) {
				throw new IllegalStateException(
						"Error duplicate subpopulation in config: " + parameters.getSubpopulation());
			}

			map.put(parameters.getSubpopulation(), parameters);
		}

		return Collections.unmodifiableMap(map);
	}

	public void addScoringParameters(AVScoringParameterSet parameters) {
		if (getScoringParameters().containsKey(parameters.getSubpopulation())) {
			throw new IllegalStateException("Subpopulation exists already: " + parameters.getSubpopulation());
		}

		addParameterSet(parameters);
	}

	public void removeScoringParameters(String subpopulation) {
		AVScoringParameterSet parameters = getScoringParameters(subpopulation);
		removeParameterSet(parameters);
	}

	public AVScoringParameterSet getScoringParameters(String subpopulation) {
		AVScoringParameterSet parameters = getScoringParameters().get(subpopulation);

		if (parameters == null) {
			throw new IllegalStateException("Missing AV scoring parameters for subpopulation: " + subpopulation);
		}

		return parameters;
	}

	public void clearScoringParameters() {
		clearParameterSetsForType(AVScoringParameterSet.GROUP_NAME);
	}

	@StringGetter(NUMBER_OF_PARALLEL_ROUTERS)
	public long getNumberOfParallelRouters() {
		return parallelRouters;
	}

	@StringSetter(NUMBER_OF_PARALLEL_ROUTERS)
	public void setNumberOfParallelRouters(long parallelRouters) {
		this.parallelRouters = parallelRouters;
	}

	@StringGetter(ACCESS_EGRESS_TYPE)
	public AccessEgressType getAccessEgressType() {
		return accessEgressType;
	}

	@StringSetter(ACCESS_EGRESS_TYPE)
	public void setAccessEgressType(AccessEgressType accessEgressType) {
		this.accessEgressType = accessEgressType;
	}

	@StringGetter(ACCESS_EGRESS_LINK_FLAG)
	public String getAccessEgressLinkFlag() {
		return accessEgressLinkFlag;
	}

	@StringSetter(ACCESS_EGRESS_LINK_FLAG)
	public void setAccessEgressLinkFlag(String accessEgressLinkFlag) {
		this.accessEgressLinkFlag = accessEgressLinkFlag;
	}

	@Override
	protected void checkConsistency(Config config) {
		super.checkConsistency(config);
		new AVConfigConsistencyChecker().checkConsistency(config);
	}

	static public AVConfigGroup getOrCreate(Config config) {
		AVConfigGroup configGroup = (AVConfigGroup) config.getModules().get(GROUP_NAME);

		if (configGroup == null) {
			configGroup = new AVConfigGroup();
			config.addModule(configGroup);
		}

		return configGroup;
	}
}
