package ch.ethz.matsim.av.scoring;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;

@Singleton
public class AVScoringFunctionFactory implements ScoringFunctionFactory {
	final private AVConfigGroup config;
	final private ScoringFunctionFactory delegate;
	final private ScoringParametersForPerson defaultParameters;
	final private AVSubpopulationScoringParameters avParameters;

	@Inject
	public AVScoringFunctionFactory(Scenario scenario, ScoringParametersForPerson defaultParameters,
			AVSubpopulationScoringParameters avParameters, AVConfigGroup config) {
		this.config = config;
		this.defaultParameters = defaultParameters;
		this.avParameters = avParameters;

		delegate = new CharyparNagelScoringFunctionFactory(scenario);
	}

	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		SumScoringFunction sf = (SumScoringFunction) delegate.createNewScoringFunction(person);

		ScoringParameters personDefaultParameters = defaultParameters.getScoringParameters(person);
		AVScoringParameters personAvParameters = avParameters.getScoringParameters(person);

		double marginalUtilityOfMoney = personDefaultParameters.marginalUtilityOfMoney;
		double marginalUtilityOfTraveling = personDefaultParameters.modeParams
				.get(AVModule.AV_MODE).marginalUtilityOfTraveling_s;
		double marginalUtilityOfWaiting = personAvParameters.marginalUtilityOfWaiting_s;
		double stuckUtility = personAvParameters.stuckUtility;

		sf.addScoringFunction(new AVScoringFunction(config, marginalUtilityOfMoney, marginalUtilityOfTraveling,
				marginalUtilityOfWaiting, stuckUtility));

		return sf;
	}
}
