package ch.ethz.matsim.av.electric;

import ch.ethz.matsim.av.electric.calculators.BinnedChargeCalculatorConfig;
import ch.ethz.matsim.av.electric.calculators.BinnedChargeCalculatorModule;
import ch.ethz.matsim.av.electric.calculators.StaticChargeCalculatorConfig;
import ch.ethz.matsim.av.electric.calculators.StaticChargeCalculatorModule;
import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimProvider;
import ch.ethz.matsim.av.scenario.TestScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.dynagent.run.DynQSimModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.EndtimeInterpretation;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunElectricTest {
    @Test
    public void testElectricAVs() {
        AVConfigGroup avConfigGroup = new AVConfigGroup();
        avConfigGroup.setConfigURL(getClass().getResource("/ch/ethz/matsim/av/electric/recharging_av_static.xml"));

        RechargingConfig rechargingConfig = new RechargingConfig();
        rechargingConfig.setTrackConsumption(true);
        rechargingConfig.setTrackingStartTime(0);
        rechargingConfig.setTrackingEndTime(3600 * 10);
        rechargingConfig.setTrackingBinDuration(300);

        StaticChargeCalculatorConfig calculatorConfig = new StaticChargeCalculatorConfig();
        calculatorConfig.setDischargeRateByDistance(0.17);
        calculatorConfig.setDischargeRateByTime(1.0);
        calculatorConfig.setMaximumCharge(19.2);
        calculatorConfig.setMinimumCharge(1.77);
        calculatorConfig.setRechargeRatePerTime(43.0);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup(), rechargingConfig, calculatorConfig, new BinnedChargeCalculatorConfig());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
        
        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpTravelTimeModule());
        controler.addOverridingModule(new DynQSimModule<>(AVQSimProvider.class));
        controler.addOverridingModule(new AVModule());

        controler.addOverridingModule(new RechargingModule());
        controler.addOverridingModule(new StaticChargeCalculatorModule());
        controler.addOverridingModule(new BinnedChargeCalculatorModule());

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        ElectricAnalyzer electricAnalyzer = new ElectricAnalyzer();
        controler.addOverridingModule(electricAnalyzer);

        controler.run();

        Assert.assertTrue(electricAnalyzer.numberOfRechargingActivities > 0);
        Assert.assertTrue(analyzer.numberOfDepartures > 0);
        Assert.assertTrue(analyzer.numberOfArrivals > 0);
    }
}
