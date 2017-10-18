package ch.ethz.matsim.av.electric.logic.action;

import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.VrpLegs.LegCreator;

import ch.ethz.matsim.av.vrpagent.AVActionCreatorFactory;

public class RechargeActionCreatorFactory implements AVActionCreatorFactory {
	final private AVActionCreatorFactory delegate;
	
	public RechargeActionCreatorFactory(AVActionCreatorFactory delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public DynActionCreator createActionCreator(PassengerEngine passengerEngine, LegCreator legCreator,
			double pickupDuration) {
		return new RechargeActionCreator(delegate.createActionCreator(passengerEngine, legCreator, pickupDuration));
	}
}
