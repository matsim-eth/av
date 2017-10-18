package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.VrpLegs.LegCreator;

public class DefaultAVActionCreatorFactory implements AVActionCreatorFactory {
	@Override
	public DynActionCreator createActionCreator(PassengerEngine passengerEngine, LegCreator legCreator,
			double pickupDuration) {
		return new AVActionCreator(passengerEngine, legCreator, pickupDuration);
	}
}
