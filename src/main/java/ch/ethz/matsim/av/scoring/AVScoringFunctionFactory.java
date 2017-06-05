package ch.ethz.matsim.av.scoring;

import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.framework.AVModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.scoring.functions.SubpopulationScoringParameters;

@Singleton
public class AVScoringFunctionFactory implements ScoringFunctionFactory {
	final private AVConfig config;
	final private ScoringFunctionFactory standardFactory;
	final private ScoringParametersForPerson params;

	@Inject
    public AVScoringFunctionFactory(Scenario scenario, AVConfig config) {
		this.config = config;
        params = new SubpopulationScoringParameters(scenario);
        standardFactory = new CharyparNagelScoringFunctionFactory(scenario);
    }
    
	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		SumScoringFunction sf = (SumScoringFunction) standardFactory.createNewScoringFunction(person);

		double marginalUtilityOfMoney = params.getScoringParameters(person).marginalUtilityOfMoney;
		double marginalUtilityOfTraveling = params.getScoringParameters(person)
                .modeParams.get(AVModule.AV_MODE).marginalUtilityOfTraveling_s;

        sf.addScoringFunction(new AVScoringFunction(config, person, marginalUtilityOfMoney, marginalUtilityOfTraveling));

		return sf;
	}
}
