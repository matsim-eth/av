package ch.ethz.matsim.av.routing;

import ch.ethz.matsim.av.framework.AVModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;
import playground.sebhoerl.plcpc.ParallelLeastCostPathCalculatorFactory;

@Singleton
public class AVParallelRouterFactory implements ParallelLeastCostPathCalculatorFactory {
    @Inject @Named(AVModule.AV_MODE) TravelTime travelTime;
    @Inject Network network;

    @Override
    public LeastCostPathCalculator createRouter() {
        return new Dijkstra(network, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);
    }
}
