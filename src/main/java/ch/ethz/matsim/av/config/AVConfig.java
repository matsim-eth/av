package ch.ethz.matsim.av.config;

import java.util.HashSet;
import java.util.Set;

import org.matsim.core.config.ReflectiveConfigGroup;

public class AVConfig extends ReflectiveConfigGroup {
	final static String MARGINAL_UTILITY_OF_WAITING_TIME = "marginalUtilityOfWaitingTime";
	final static String STUCK_UTILITY = "stuckUtility";

	final static String AV = "av";

	final private Set<AVOperatorConfig> operators = new HashSet<>();
	final private AVTimingParameters timingParameters = AVTimingParameters.createDefault();

	private double marginalUtilityOfWaitingTime = 0.0;
	private double stuckUtility = -1000.0;

	public AVConfig() {
		super(AV);
	}

	public Set<AVOperatorConfig> getOperatorConfigs() {
		return operators;
	}

	public AVTimingParameters getTimingParameters() {
		return timingParameters;
	}

	public AVOperatorConfig createOperatorConfig(String id) {
		AVOperatorConfig oc = new AVOperatorConfig(id, this);
		operators.add(oc);
		return oc;
	}

	@StringGetter(MARGINAL_UTILITY_OF_WAITING_TIME)
	public double getMarginalUtilityOfWaitingTime() {
		return marginalUtilityOfWaitingTime;
	}

	@StringSetter(MARGINAL_UTILITY_OF_WAITING_TIME)
	public void setMarginalUtilityOfWaitingTime(double marginalUtilityOfWaitingTime) {
		this.marginalUtilityOfWaitingTime = marginalUtilityOfWaitingTime;
	}

	@StringGetter(STUCK_UTILITY)
	public double getStuckUtility() {
		return stuckUtility;
	}

	@StringSetter(STUCK_UTILITY)
	public void setStuckUtility(double stuckUtility) {
		this.stuckUtility = stuckUtility;
	}
}
