package ch.ethz.matsim.av.passenger;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.RequestImpl;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

public class AVRequest extends RequestImpl implements PassengerRequest {
    final private Link pickupLink;
    final private Link dropoffLink;
    final private MobsimPassengerAgent passengerAgent;
    final private AVOperator operator;
    final private AVDispatcher dispatcher;
    final private AVRoute route;

    private AVPickupTask pickupTask;
    private AVDropoffTask dropoffTask;

    public AVRequest(Id<Request> id, MobsimPassengerAgent passengerAgent, Link pickupLink, Link dropoffLink, double pickupTime, double submissionTime, AVRoute route, AVOperator operator, AVDispatcher dispatcher) {
        super(id, 1.0, pickupTime, pickupTime, submissionTime);

        this.passengerAgent = passengerAgent;
        this.pickupLink = pickupLink;
        this.dropoffLink = dropoffLink;
        this.operator = operator;
        this.route = route;
        this.dispatcher = dispatcher;
    }

    @Override
    public Link getFromLink() {
        return pickupLink;
    }

    @Override
    public Link getToLink() {
        return dropoffLink;
    }

    @Override
    public MobsimPassengerAgent getPassenger() {
        return passengerAgent;
    }

    public AVPickupTask getPickupTask() {
        return pickupTask;
    }

    public void setPickupTask(AVPickupTask pickupTask) {
        this.pickupTask = pickupTask;
    }

    public AVDropoffTask getDropoffTask() {
        return dropoffTask;
    }

    public void setDropoffTask(AVDropoffTask dropoffTask) {
        this.dropoffTask = dropoffTask;
    }

    public AVOperator getOperator() {
        return operator;
    }

    public AVDispatcher getDispatcher() {
        return dispatcher;
    }

    public AVRoute getRoute() {
        return route;
    }
}
