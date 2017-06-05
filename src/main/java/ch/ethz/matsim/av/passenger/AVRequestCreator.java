package ch.ethz.matsim.av.passenger;

import ch.ethz.matsim.av.data.AVData;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.routing.AVRoute;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;
import org.matsim.core.mobsim.framework.PlanAgent;

import java.util.Map;

@Singleton
public class AVRequestCreator implements PassengerRequestCreator {
    @Inject
    AVData data;

    @Inject
    Map<Id<AVOperator>, AVOperator> operators;

    @Inject
    Map<Id<AVOperator>, AVDispatcher> dispatchers;

    @Override
    public PassengerRequest createRequest(Id<Request> id, MobsimPassengerAgent passenger, Link pickupLink, Link dropoffLink, double departureTime, double submissionTime) {
        if (!(passenger instanceof PlanAgent)) {
            throw new RuntimeException("Need PlanAgent in order to figure out the operator");
        }

        PlanAgent agent = (PlanAgent) passenger;
        Leg leg = (Leg) agent.getCurrentPlanElement();

        AVRoute route = (AVRoute) leg.getRoute();
        route.setDistance(Double.NaN);

        AVOperator operator = operators.get(route.getOperatorId());

        if (operator == null) {
            throw new IllegalStateException("Operator '" + route.getOperatorId().toString() + "' does not exist.");
        }

        return new AVRequest(id, passenger, pickupLink, dropoffLink, departureTime, submissionTime, route, operator, dispatchers.get(route.getOperatorId()));
    }
}
