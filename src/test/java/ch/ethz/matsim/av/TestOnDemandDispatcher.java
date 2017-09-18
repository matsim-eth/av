package ch.ethz.matsim.av;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.VrpTravelTimeModules;
import org.matsim.contrib.dynagent.run.DynQSimModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;

import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimProvider;
import ch.ethz.matsim.av.scenario.TestScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class TestOnDemandDispatcher {
	@Test
	public void testOnDemandDispatcher() {
        AVConfigGroup avConfigGroup = new AVConfigGroup();
        avConfigGroup.setConfigURL(getClass().getResource("/ch/ethz/matsim/av/ondemand.xml"));

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
        
        config.controler().setWriteEventsInterval(1);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(VrpTravelTimeModules.createTravelTimeEstimatorModule());
        controler.addOverridingModule(new DynQSimModule<>(AVQSimProvider.class));
        controler.addOverridingModule(new AVModule());

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
	}
}
