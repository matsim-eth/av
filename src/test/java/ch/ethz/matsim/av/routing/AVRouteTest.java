package ch.ethz.matsim.av.routing;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.matsim.av.data.AVOperator;

public class AVRouteTest {
	@Test
	public void testReadWriteRoute() {
		{
			// Writing file
			Config config = ConfigUtils.createConfig();
			Scenario scenario = ScenarioUtils.createScenario(config);

			Population population = scenario.getPopulation();
			PopulationFactory factory = population.getFactory();

			Person person = factory.createPerson(Id.createPersonId("person"));
			population.addPerson(person);

			Plan plan = factory.createPlan();
			person.addPlan(plan);

			AVRouteFactory routeFactory = new AVRouteFactory();

			Leg leg;
			AVRoute route;

			plan.addActivity(factory.createActivityFromLinkId("whatever", Id.createLinkId("somewhere")));

			leg = factory.createLeg("av");
			route = routeFactory.createRoute(Id.createLinkId("S1"), Id.createLinkId("E1"));
			route.setOperatorId(Id.create("O1", AVOperator.class));
			route.setWaitingTime(123.0);
			leg.setRoute(route);
			plan.addLeg(leg);

			plan.addActivity(factory.createActivityFromLinkId("whatever", Id.createLinkId("somewhere")));

			leg = factory.createLeg("av");
			route = routeFactory.createRoute(Id.createLinkId("S2"), Id.createLinkId("E2"));
			route.setOperatorId(Id.create("O2", AVOperator.class));
			route.setWaitingTime(987.0);
			leg.setRoute(route);
			plan.addLeg(leg);

			plan.addActivity(factory.createActivityFromLinkId("whatever", Id.createLinkId("somewhere")));

			new PopulationWriter(population).write("test_population.xml.gz");
		}

		{
			// Reading file
			Config config = ConfigUtils.createConfig();
			Scenario scenario = ScenarioUtils.createScenario(config);

			Population population = scenario.getPopulation();
			population.getFactory().getRouteFactories().setRouteFactory(AVRoute.class, new AVRouteFactory());
			new PopulationReader(scenario).readFile("test_population.xml.gz");

			Person person = population.getPersons().values().iterator().next();
			Plan plan = person.getPlans().get(0);

			Leg leg1 = (Leg) plan.getPlanElements().get(1);
			Leg leg2 = (Leg) plan.getPlanElements().get(3);

			AVRoute route1 = (AVRoute) leg1.getRoute();
			AVRoute route2 = (AVRoute) leg2.getRoute();

			Assert.assertEquals("S1", route1.getStartLinkId().toString());
			Assert.assertEquals("E1", route1.getEndLinkId().toString());
			Assert.assertEquals("O1", route1.getOperatorId().toString());
			Assert.assertEquals(123.0, route1.getWaitingTime(), 1e-3);

			Assert.assertEquals("S2", route2.getStartLinkId().toString());
			Assert.assertEquals("E2", route2.getEndLinkId().toString());
			Assert.assertEquals("O2", route2.getOperatorId().toString());
			Assert.assertEquals(987.0, route2.getWaitingTime(), 1e-3);
		}
	}
}
