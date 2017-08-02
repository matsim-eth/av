package ch.ethz.matsim.av;

import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimProvider;
import ch.ethz.matsim.av.scenario.ScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.events.handler.PersonStuckEventHandler;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.VrpTravelTimeModules;
import org.matsim.contrib.dynagent.run.DynQSimModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;

public class RunAVExampleTest {
    @Test
    public void testAVExample() {
        AVConfigGroup avConfigGroup = new AVConfigGroup();
        avConfigGroup.setConfigURL(getClass().getResource("/ch/ethz/matsim/av/av.xml"));

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(VrpTravelTimeModules.createTravelTimeEstimatorModule());
        controler.addOverridingModule(new DynQSimModule<>(AVQSimProvider.class));
        controler.addOverridingModule(new AVModule());

        ScenarioAnalyzer analyzer = new ScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
    }
}
