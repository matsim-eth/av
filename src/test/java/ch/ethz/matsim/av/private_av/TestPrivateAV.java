package ch.ethz.matsim.av.private_av;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.VrpTravelTimeModules;
import org.matsim.contrib.dynagent.run.DynQSimModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;

import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimProvider;
import ch.ethz.matsim.av.scenario.TestScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class TestPrivateAV {
	@Test
	public void testPrivateAV() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();
		avConfigGroup.setConfigURL(getClass().getResource("/ch/ethz/matsim/av/private_av/private.xml"));

		Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());

		Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
		scenario.getPopulation().getPersons().clear();
		
		PopulationFactory populationFactory = scenario.getPopulation().getFactory();
		
		Activity activity1 = populationFactory.createActivityFromLinkId("home", Id.createLinkId("1:8_2:8"));
		Activity activity2 = populationFactory.createActivityFromLinkId("work", Id.createLinkId("1:2_1:1"));
		Activity activity3 = populationFactory.createActivityFromLinkId("shop", Id.createLinkId("8:2_8:3"));
		Activity activity4 = populationFactory.createActivityFromLinkId("home", Id.createLinkId("6:6_6:7"));
		
		for (Activity activity : new Activity[] { activity1, activity2, activity3, activity4 }) {
			activity.setCoord(scenario.getNetwork().getLinks().get(activity.getLinkId()).getCoord());
		}

		Leg leg12 = populationFactory.createLeg("av");
		Leg leg23 = populationFactory.createLeg("walk");
		Leg leg34 = populationFactory.createLeg("av");

		activity1.setEndTime(1 * 3600.0);
		activity2.setEndTime(2 * 3600.0);
		activity3.setEndTime(3 * 3600.0);

		Plan plan = populationFactory.createPlan();
		plan.addActivity(activity1);
		plan.addLeg(leg12);
		plan.addActivity(activity2);
		plan.addLeg(leg23);
		plan.addActivity(activity3);
		plan.addLeg(leg34);
		plan.addActivity(activity4);

		Person person = populationFactory.createPerson(Id.createPersonId("person1"));
		person.addPlan(plan);

		scenario.getPopulation().addPerson(person);

		PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
		modeParams.setMonetaryDistanceRate(0.0);
		modeParams.setMarginalUtilityOfTraveling(8.86);
		modeParams.setConstant(0.0);
		
		config.planCalcScore().addActivityParams(new ActivityParams("work"));
		config.planCalcScore().addActivityParams(new ActivityParams("home"));
		config.planCalcScore().addActivityParams(new ActivityParams("shop"));
		config.planCalcScore().addActivityParams(new ActivityParams("remote"));

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(VrpTravelTimeModules.createTravelTimeEstimatorModule());
		controler.addOverridingModule(new DynQSimModule<>(AVQSimProvider.class));
		controler.addOverridingModule(new AVModule());
		controler.addOverridingModule(new PrivateModule());

		TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
		controler.addOverridingModule(analyzer);

		controler.run();

		Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
	}
}
