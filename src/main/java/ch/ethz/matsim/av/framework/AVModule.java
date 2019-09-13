package ch.ethz.matsim.av.framework;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.dvrp.passenger.DefaultPassengerRequestValidator;
import org.matsim.contrib.dvrp.passenger.PassengerRequestValidator;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.AVConfigGroup.AccessEgressType;
import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.data.AVOperatorFactory;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.MultiODHeuristic;
import ch.ethz.matsim.av.dispatcher.single_fifo.SingleFIFODispatcher;
import ch.ethz.matsim.av.dispatcher.single_heuristic.SingleHeuristicDispatcher;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.generator.PopulationDensityGenerator;
import ch.ethz.matsim.av.replanning.AVOperatorChoiceStrategy;
import ch.ethz.matsim.av.router.AVRouter;
import ch.ethz.matsim.av.router.AVRouterShutdownListener;
import ch.ethz.matsim.av.router.DefaultAVRouter;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.routing.AVRouteFactory;
import ch.ethz.matsim.av.routing.AVRoutingModule;
import ch.ethz.matsim.av.routing.interaction.AVInteractionFinder;
import ch.ethz.matsim.av.routing.interaction.InteractionDataFinder;
import ch.ethz.matsim.av.routing.interaction.InteractionLinkData;
import ch.ethz.matsim.av.routing.interaction.ModeInteractionFinder;
import ch.ethz.matsim.av.scoring.AVScoringFunctionFactory;
import ch.ethz.matsim.av.scoring.AVSubpopulationScoringParameters;

public class AVModule extends AbstractModule {
	final static public String AV_MODE = "av";
	final static Logger log = Logger.getLogger(AVModule.class);

	private final boolean addQSimModule;

	public AVModule() {
		addQSimModule = true;
	}

	// Only for compatibility with Amodeus
	public AVModule(boolean addQSimModule) {
		this.addQSimModule = addQSimModule;
	}

	@Override
	public void install() {
		AVConfigGroup config = (AVConfigGroup) getConfig().getModules().get(AVConfigGroup.GROUP_NAME);

		bind(DvrpModes.key(PassengerRequestValidator.class, AV_MODE))
				.toInstance(new DefaultPassengerRequestValidator());

		if (addQSimModule) {
			installQSimModule(new AVQSimModule());
		}

		addRoutingModuleBinding(AV_MODE).to(AVRoutingModule.class);
		bind(ScoringFunctionFactory.class).to(AVScoringFunctionFactory.class).asEagerSingleton();

		bind(AVOperatorChoiceStrategy.class);
		addPlanStrategyBinding("AVOperatorChoice").to(AVOperatorChoiceStrategy.class);

		// Bind the AV travel time to the DVRP estimated travel time
		bind(TravelTime.class).annotatedWith(Names.named(AVModule.AV_MODE))
				.to(Key.get(TravelTime.class, Names.named(DvrpTravelTimeModule.DVRP_ESTIMATED)));

		bind(VehicleType.class).annotatedWith(Names.named(AVModule.AV_MODE))
				.toInstance(VehicleUtils.getDefaultVehicleType());

		bind(AVOperatorFactory.class);
		bind(AVRouteFactory.class);
		addRoutingModuleBinding(AV_MODE).to(AVRoutingModule.class);

		switch (config.getAccessEgressType()) {
		case ATTRIBUTE:
			bind(AVInteractionFinder.class).to(InteractionDataFinder.class);
			bind(InteractionLinkData.class).to(Key.get(InteractionLinkData.class, Names.named("attribute")));
			break;
		case MODE:
		case NONE:
			bind(AVInteractionFinder.class).to(ModeInteractionFinder.class);
			bind(InteractionLinkData.class).to(Key.get(InteractionLinkData.class, Names.named("empty")));
			break;
		default:
			throw new IllegalStateException();
		}

		configureDispatchmentStrategies();
		configureGeneratorStrategies();

		// bind(Network.class).annotatedWith(Names.named(DvrpRoutingNetworkProvider.DVRP_ROUTING)).to(Network.class);
		bind(Network.class).annotatedWith(Names.named(AVModule.AV_MODE))
				.to(Key.get(Network.class, Names.named(DvrpRoutingNetworkProvider.DVRP_ROUTING)));

		addControlerListenerBinding().to(AVRouterShutdownListener.class);
		AVUtils.registerRouterFactory(binder(), DefaultAVRouter.TYPE, DefaultAVRouter.Factory.class);

		bind(AVSubpopulationScoringParameters.class);
	}

	private void configureDispatchmentStrategies() {
		bind(SingleFIFODispatcher.Factory.class);
		bind(SingleHeuristicDispatcher.Factory.class);
		bind(MultiODHeuristic.Factory.class);

		AVUtils.bindDispatcherFactory(binder(), SingleFIFODispatcher.TYPE).to(SingleFIFODispatcher.Factory.class);
		AVUtils.bindDispatcherFactory(binder(), SingleHeuristicDispatcher.TYPE)
				.to(SingleHeuristicDispatcher.Factory.class);
		AVUtils.bindDispatcherFactory(binder(), MultiODHeuristic.TYPE).to(MultiODHeuristic.Factory.class);
	}

	private void configureGeneratorStrategies() {
		bind(PopulationDensityGenerator.Factory.class);
		AVUtils.bindGeneratorFactory(binder(), PopulationDensityGenerator.TYPE).to(PopulationDensityGenerator.Factory.class);
	}

	@Provides
	RouteFactories provideRouteFactories(AVRouteFactory routeFactory) {
		RouteFactories factories = new RouteFactories();
		factories.setRouteFactory(AVRoute.class, routeFactory);
		return factories;
	}

	@Provides
	@Named(AVModule.AV_MODE)
	LeastCostPathCalculator provideLeastCostPathCalculator(
			@com.google.inject.name.Named(AVModule.AV_MODE) Network network,
			@Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime) {
		DijkstraFactory dijkstraFactory = new DijkstraFactory();
		return dijkstraFactory.createPathCalculator(network, new OnlyTimeDependentTravelDisutility(travelTime),
				travelTime);
	}

	@Provides
	@Singleton
	Map<Id<AVOperator>, AVOperator> provideOperators(AVConfigGroup config, AVOperatorFactory factory) {
		Map<Id<AVOperator>, AVOperator> operators = new HashMap<>();

		for (OperatorConfig oc : config.getOperators().values()) {
			operators.put(oc.getId(), factory.createOperator(oc.getId(), oc));
		}

		return operators;
	}

	@Provides
	@Singleton
	Map<Id<AVOperator>, AVGenerator> provideGenerators(Map<String, AVGenerator.AVGeneratorFactory> factories,
			AVConfigGroup config) {
		Map<Id<AVOperator>, AVGenerator> generators = new HashMap<>();

		for (OperatorConfig oc : config.getOperators().values()) {
			GeneratorConfig gc = oc.getGeneratorConfig();
			String strategy = gc.getType();

			if (!factories.containsKey(strategy)) {
				throw new IllegalArgumentException("Generator strategy '" + strategy + "' is not registered.");
			}

			AVGenerator.AVGeneratorFactory factory = factories.get(strategy);
			AVGenerator generator = factory.createGenerator(oc);

			generators.put(oc.getId(), generator);
		}

		return generators;
	}

	@Provides
	@Singleton
	public Map<Id<AVOperator>, List<AVVehicle>> provideVehicles(Map<Id<AVOperator>, AVOperator> operators,
			Map<Id<AVOperator>, AVGenerator> generators) {
		Map<Id<AVOperator>, List<AVVehicle>> vehicles = new HashMap<>();

		for (AVOperator operator : operators.values()) {
			LinkedList<AVVehicle> operatorList = new LinkedList<>();

			AVGenerator generator = generators.get(operator.getId());

			while (generator.hasNext()) {
				AVVehicle vehicle = generator.next();
				vehicle.setOpeartor(operator);
				operatorList.add(vehicle);

				if (Double.isFinite(vehicle.getServiceEndTime())) {
					throw new IllegalStateException("AV vehicles must have infinite service time");
				}
			}

			vehicles.put(operator.getId(), operatorList);
		}

		return vehicles;
	}

	@Provides
	@Singleton
	public Map<Id<AVOperator>, AVRouter> provideRouters(Map<Id<AVOperator>, AVOperator> operators,
			Map<String, AVRouter.Factory> factories) {
		Map<Id<AVOperator>, AVRouter> routers = new HashMap<>();

		for (AVOperator operator : operators.values()) {
			String routerName = operator.getConfig().getRouterConfig().getType();

			if (!factories.containsKey(routerName)) {
				throw new IllegalStateException("Router '" + routerName + "' is not registered");
			}

			routers.put(operator.getId(),
					factories.get(routerName).createRouter(operator.getConfig().getRouterConfig()));
		}

		return routers;
	}

	@Provides
	public AVRoutingModule provideAVRoutingModule(AVOperatorChoiceStrategy choiceStrategy, AVRouteFactory routeFactory,
			AVInteractionFinder interactionFinder, PopulationFactory populationFactory,
			@Named("walk") RoutingModule walkRoutingModule, AVConfigGroup config) {
		return new AVRoutingModule(choiceStrategy, routeFactory, interactionFinder, populationFactory,
				walkRoutingModule, !config.getAccessEgressType().equals(AccessEgressType.NONE));
	}

	@Provides
	public ModeInteractionFinder provideModeInteractionFinder(@Named(AV_MODE) Network network) {
		return new ModeInteractionFinder(network);
	}

	@Provides
	@Singleton
	@Named("attribute")
	public InteractionLinkData provideAttributeInteractionLinkData(@Named(AV_MODE) Network network,
			AVConfigGroup config) {
		return InteractionLinkData.fromAttribute(config.getAccessEgressLinkFlag(), network);
	}

	@Provides
	@Singleton
	@Named("empty")
	public InteractionLinkData provideEmptyInteractionLinkData() {
		return InteractionLinkData.empty();
	}

	@Provides
	public InteractionDataFinder provideInteractionDataFinder(InteractionLinkData data) {
		return new InteractionDataFinder(data);
	}
}
