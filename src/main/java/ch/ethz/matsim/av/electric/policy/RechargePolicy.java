package ch.ethz.matsim.av.electric.policy;

import java.util.Map;
import java.util.Set;

import ch.ethz.matsim.av.data.AVVehicle;

public interface RechargePolicy {
	boolean sendToRecharge(AVVehicle vehicle, double chargeState);
	void informChargeState(double now, Map<AVVehicle, Double> chargeState, Set<AVVehicle> chargingVehicles);
}
