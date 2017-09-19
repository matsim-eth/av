package ch.ethz.matsim.av.passenger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;
import org.matsim.core.mobsim.qsim.QSim;

import com.google.inject.Singleton;

import ch.ethz.matsim.av.routing.AVRoute;

@Singleton
public class OnlineRequestCreator {
	private QSim qsim = null;
	private AVRequestCreator requestCreator = null;
	private PassengerEngine passengerEngine = null;

	public void update(QSim qsim, PassengerEngine passengerEngine, AVRequestCreator requestCreator) {
		this.qsim = qsim;
		this.requestCreator = requestCreator;
		this.passengerEngine = passengerEngine;
	}
	
	public AVRequest createRequest(Id<Request> requestId, Id<Person> personId, Link pickupLink, Link dropoffLink, double departureTime, AVRoute route) {
		MobsimPassengerAgent passenger = (MobsimPassengerAgent) qsim.getAgents().get(personId);
		//passengerEngine.prebookTrip(0.0, passenger, pickupLink.getId(), dropoffLink.getId(), departureTime);
		return requestCreator.createRequest(requestId, passenger, pickupLink, dropoffLink, departureTime, 0.0, route);
	}
}
