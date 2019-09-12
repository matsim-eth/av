package ch.ethz.matsim.av.dynamics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.QSimConfigGroup.StarttimeInterpretation;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import com.google.inject.Provides;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.config.AVOperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVConfigGroup.AccessEgressType;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.framework.AVUtils;
import ch.ethz.matsim.av.generator.AVGenerator;

public class AVPickupDropoffTest {
	private static class TestRequest {
		private final double departureTime;
		private final double delayTime;

		public TestRequest(double departureTime, double delayTime) {
			this.departureTime = departureTime;
			this.delayTime = delayTime;
		}
	}

	static private Scenario createScenario(Collection<TestRequest> requests) {
		AVConfigGroup avConfig = new AVConfigGroup();
		avConfig.setAccessEgressType(AccessEgressType.MODE);

		DvrpConfigGroup dvrpConfigGroup = new DvrpConfigGroup();
		dvrpConfigGroup.setNetworkMode("av");

		Config config = ConfigUtils.createConfig(avConfig, dvrpConfigGroup);

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(0);
		config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

		config.qsim().setFlowCapFactor(1000.0);
		config.qsim().setEndTime(10 * 3600.0);

		ModeParams modeParams = new ModeParams("av");
		config.planCalcScore().addModeParams(modeParams);

		ActivityParams activityParams = new ActivityParams("av interaction");
		activityParams.setTypicalDuration(1.0);
		activityParams.setScoringThisActivityAtAll(false);
		config.planCalcScore().addActivityParams(activityParams);

		activityParams = new ActivityParams("generic");
		activityParams.setTypicalDuration(1.0);
		activityParams.setScoringThisActivityAtAll(false);
		config.planCalcScore().addActivityParams(activityParams);

		config.plansCalcRoute().getModeRoutingParams().get("walk").setTeleportedModeSpeed(1.0);
		config.plansCalcRoute().getModeRoutingParams().get("walk").setBeelineDistanceFactor(1.0);

		Scenario scenario = ScenarioUtils.createScenario(config);

		Network network = scenario.getNetwork();
		NetworkFactory networkFactory = network.getFactory();

		Node node1 = networkFactory.createNode(Id.createNodeId("node1"), new Coord(0.0, 0.0));
		Node node2 = networkFactory.createNode(Id.createNodeId("node2"), new Coord(10000.0, 0.0));
		Node node3 = networkFactory.createNode(Id.createNodeId("node3"), new Coord(20000.0, 0.0));
		Node node4 = networkFactory.createNode(Id.createNodeId("node4"), new Coord(30000.0, 0.0));

		Link link1 = networkFactory.createLink(Id.createLinkId("link1"), node1, node2);
		Link link2 = networkFactory.createLink(Id.createLinkId("link2"), node2, node3);
		Link link3 = networkFactory.createLink(Id.createLinkId("link3"), node3, node4);
		Link link1r = networkFactory.createLink(Id.createLinkId("link1r"), node2, node1);
		Link link2r = networkFactory.createLink(Id.createLinkId("link2r"), node3, node2);
		Link link3r = networkFactory.createLink(Id.createLinkId("link3r"), node4, node3);

		link1.setAllowedModes(Collections.singleton("av"));
		link2.setAllowedModes(Collections.singleton("av"));
		link3.setAllowedModes(Collections.singleton("av"));
		link1r.setAllowedModes(Collections.singleton("av"));
		link2r.setAllowedModes(Collections.singleton("av"));
		link3r.setAllowedModes(Collections.singleton("av"));

		link1.setFreespeed(10.0);
		link2.setFreespeed(10.0);
		link3.setFreespeed(10.0);
		link1r.setFreespeed(10.0);
		link2r.setFreespeed(10.0);
		link3r.setFreespeed(10.0);

		network.addNode(node1);
		network.addNode(node2);
		network.addNode(node3);
		network.addNode(node4);

		network.addLink(link1);
		network.addLink(link2);
		network.addLink(link3);
		network.addLink(link1r);
		network.addLink(link2r);
		network.addLink(link3r);

		Population population = scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();
		int personIndex = 0;

		for (TestRequest request : requests) {
			Person person = populationFactory.createPerson(Id.createPersonId(personIndex++));
			population.addPerson(person);

			Plan plan = populationFactory.createPlan();
			person.addPlan(plan);

			Coord startCoord = link1.getCoord();
			startCoord = CoordUtils.plus(startCoord, new Coord(-request.delayTime, 0.0));

			Activity firstActivity = populationFactory.createActivityFromLinkId("generic", link1.getId());
			firstActivity.setCoord(startCoord);
			firstActivity.setEndTime(request.departureTime);
			plan.addActivity(firstActivity);

			Leg leg = populationFactory.createLeg("av");
			plan.addLeg(leg);

			Activity secondActivity = populationFactory.createActivityFromLinkId("generic", link3.getId());
			secondActivity.setCoord(link2.getCoord());
			plan.addActivity(secondActivity);
		}

		return scenario;
	}

	static private class SingleVehicleGenerator implements AVGenerator {
		private final int capacity;
		private final Link link;
		private boolean created = false;

		public SingleVehicleGenerator(Link link, int capacity) {
			this.link = link;
			this.capacity = capacity;
		}

		@Override
		public boolean hasNext() {
			return !created;
		}

		@Override
		public AVVehicle next() {
			created = true;
			return new AVVehicle(Id.create("vehicle", Vehicle.class), link, capacity, 0.0,
					Double.POSITIVE_INFINITY);
		}
	}

	static private class SingleVehicleGeneratorFactory implements AVGenerator.AVGeneratorFactory {
		private final int capacity;
		private final Link link;

		public SingleVehicleGeneratorFactory(int capacity, Link link) {
			this.capacity = capacity;
			this.link = link;
		}

		@Override
		public AVGenerator createGenerator(AVGeneratorConfig generatorConfig) {
			return new SingleVehicleGenerator(link, capacity);
		}
	}

	static private AVConfig createConfig() {
		AVConfig config = new AVConfig();

		AVOperatorConfig operatorConfig = config.createOperatorConfig("op1");
		operatorConfig.createDispatcherConfig("MultiOD");
		operatorConfig.createGeneratorConfig("Single");

		return config;
	}

	static private Controler createController(Scenario scenario, EventHandler handler, AVConfig config,
			int vehicleCapacity) {
		Controler controller = new Controler(scenario);

		controller.addOverridingModule(new DvrpTravelTimeModule());
		controller.addOverridingModule(new AVModule());

		controller.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				AVUtils.registerGeneratorFactory(binder(), "Single", SingleVehicleGeneratorFactory.class);
				bind(AVConfig.class).toInstance(config);
				addEventHandlerBinding().toInstance(handler);
			}

			@Provides
			public SingleVehicleGeneratorFactory provideFactory(@Named("av") Network network) {
				return new SingleVehicleGeneratorFactory(vehicleCapacity,
						network.getLinks().get(Id.createLinkId("link1")));
			}
		});

		return controller;
	}

	private static class ArrivalListener implements PersonArrivalEventHandler {
		public List<Double> times = new LinkedList<>();

		@Override
		public void handleEvent(PersonArrivalEvent event) {
			if (!event.getPersonId().toString().equals("vehicle") && event.getLegMode().equals("av")) {
				times.add(event.getTime());
			}
		}
	}

	@Test
	public void testNoPickupTime() {
		{ // One agent, no pickup time
			Scenario scenario = createScenario(Arrays.asList(new TestRequest(0.0, 0.0)));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(0.0);
			config.getTimingParameters().setPickupDurationPerStop(0.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 1);
			controller.run();

			Assert.assertEquals(1, listener.times.size());
			Assert.assertEquals(1012.0, listener.times.get(0), 1e-3);
		}

		{ // Three agents, no pickup time
			Scenario scenario = createScenario(Arrays.asList( //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0) //
			));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(0.0);
			config.getTimingParameters().setPickupDurationPerStop(0.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 4);
			controller.run();

			Assert.assertEquals(3, listener.times.size());
			Assert.assertEquals(1012.0, listener.times.get(0), 1e-3);
			Assert.assertEquals(1012.0, listener.times.get(1), 1e-3);
			Assert.assertEquals(1012.0, listener.times.get(2), 1e-3);
		}
	}

	@Test
	public void testPickupTimePerStop() {
		{ // One agent, 15s pickup time per stop
			Scenario scenario = createScenario(Arrays.asList(new TestRequest(0.0, 0.0)));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(0.0);
			config.getTimingParameters().setPickupDurationPerStop(15.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 1);
			controller.run();

			Assert.assertEquals(1, listener.times.size());
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(0), 1e-3);
		}

		{ // Three agents, 15s pickup time per stop
			Scenario scenario = createScenario(Arrays.asList( //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0) //
			));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(0.0);
			config.getTimingParameters().setPickupDurationPerStop(15.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 4);
			controller.run();

			Assert.assertEquals(3, listener.times.size());
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(0), 1e-3);
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(1), 1e-3);
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(2), 1e-3);
		}
	}

	@Test
	public void testPickupTimePerPerson() {
		{ // One agent, 15s pickup time per person
			Scenario scenario = createScenario(Arrays.asList(new TestRequest(0.0, 0.0)));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(15.0);
			config.getTimingParameters().setPickupDurationPerStop(0.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 1);
			controller.run();

			Assert.assertEquals(1, listener.times.size());
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(0), 1e-3);
		}

		{ // Three agents, 15s pickup time per person
			Scenario scenario = createScenario(Arrays.asList( //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0) //
			));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(15.0);
			config.getTimingParameters().setPickupDurationPerStop(0.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 4);
			controller.run();

			Assert.assertEquals(3, listener.times.size());
			Assert.assertEquals(1012.0 + 15.0 * 3.0, listener.times.get(0), 1e-3);
			Assert.assertEquals(1012.0 + 15.0 * 3.0, listener.times.get(1), 1e-3);
			Assert.assertEquals(1012.0 + 15.0 * 3.0, listener.times.get(2), 1e-3);
		}
	}

	@Test
	public void testWaitEmptyForPerson() {
		Scenario scenario = createScenario(Arrays.asList(new TestRequest(0.0, 100.0)));
		AVConfig config = createConfig();

		config.getTimingParameters().setPickupDurationPerPassenger(0.0);
		config.getTimingParameters().setPickupDurationPerStop(0.0);
		config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
		config.getTimingParameters().setDropoffDurationPerStop(0.0);

		ArrivalListener listener = new ArrivalListener();
		Controler controller = createController(scenario, listener, config, 1);
		controller.run();

		Assert.assertEquals(1, listener.times.size());
		Assert.assertEquals(1012.0 + 100.0, listener.times.get(0), 1e-3);
	}
	
	@Test
	public void testDropoffTime() {
		{ // Three agents, no dropoff time
			Scenario scenario = createScenario(Arrays.asList( //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0) //
			));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(0.0);
			config.getTimingParameters().setPickupDurationPerStop(0.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 4);
			controller.run();

			Assert.assertEquals(3, listener.times.size());
			Assert.assertEquals(1012.0, listener.times.get(0), 1e-3);
			Assert.assertEquals(1012.0, listener.times.get(1), 1e-3);
			Assert.assertEquals(1012.0, listener.times.get(2), 1e-3);
		}
		
		{ // Three agents, 15s dropoff time per stop
			Scenario scenario = createScenario(Arrays.asList( //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0) //
			));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(0.0);
			config.getTimingParameters().setPickupDurationPerStop(0.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(0.0);
			config.getTimingParameters().setDropoffDurationPerStop(15.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 4);
			controller.run();

			Assert.assertEquals(3, listener.times.size());
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(0), 1e-3);
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(1), 1e-3);
			Assert.assertEquals(1012.0 + 15.0, listener.times.get(2), 1e-3);
		}
		
		{ // Three agents, 15s dropoff time per passenger
			Scenario scenario = createScenario(Arrays.asList( //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0), //
					new TestRequest(0.0, 0.0) //
			));
			AVConfig config = createConfig();

			config.getTimingParameters().setPickupDurationPerPassenger(0.0);
			config.getTimingParameters().setPickupDurationPerStop(0.0);
			config.getTimingParameters().setDropoffDurationPerPassenger(15.0);
			config.getTimingParameters().setDropoffDurationPerStop(0.0);

			ArrivalListener listener = new ArrivalListener();
			Controler controller = createController(scenario, listener, config, 4);
			controller.run();

			Assert.assertEquals(3, listener.times.size());
			Assert.assertEquals(1012.0 + 15.0 * 3.0, listener.times.get(0), 1e-3);
			Assert.assertEquals(1012.0 + 15.0 * 3.0, listener.times.get(1), 1e-3);
			Assert.assertEquals(1012.0 + 15.0 * 3.0, listener.times.get(2), 1e-3);
		}
	}
}
