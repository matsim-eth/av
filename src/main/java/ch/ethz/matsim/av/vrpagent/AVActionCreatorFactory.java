package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpLegs;

public interface AVActionCreatorFactory {
	VrpAgentLogic.DynActionCreator createActionCreator(PassengerEngine passengerEngine, VrpLegs.LegCreator legCreator, double pickupDuration);
}
