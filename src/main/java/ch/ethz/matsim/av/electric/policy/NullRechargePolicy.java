package ch.ethz.matsim.av.electric.policy;

import java.util.Map;
import java.util.Set;

import ch.ethz.matsim.av.data.AVVehicle;

public class NullRechargePolicy implements RechargePolicy {
	@Override
	public boolean sendToRecharge(AVVehicle vehicle, double chargeState) {
		return false;
	}

	@Override
	public void informChargeState(double now, Map<AVVehicle, Double> chargeState, Set<AVVehicle> chargingVehicles) {
		// Do nothing.
	}
}
