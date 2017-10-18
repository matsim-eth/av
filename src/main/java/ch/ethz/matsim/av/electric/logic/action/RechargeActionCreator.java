package ch.ethz.matsim.av.electric.logic.action;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynAgent;

import ch.ethz.matsim.av.electric.assets.vehicle.BatteryVehicle;
import ch.ethz.matsim.av.electric.logic.tasks.RechargeTask;
import ch.ethz.matsim.av.electric.logic.tasks.TaskWithStation;
import ch.ethz.matsim.av.electric.logic.tasks.WaitForRechargeTask;

public class RechargeActionCreator implements VrpAgentLogic.DynActionCreator {
	final private VrpAgentLogic.DynActionCreator delegate;
	
	public RechargeActionCreator(VrpAgentLogic.DynActionCreator delegate) {
		this.delegate = delegate;
	}

	@Override
	public DynAction createAction(DynAgent dynAgent, Vehicle vehicle, double now) {
		Task task = vehicle.getSchedule().getCurrentTask();
		
		if (task instanceof TaskWithStation && vehicle instanceof BatteryVehicle) {
			if (task instanceof WaitForRechargeTask) {
				return new WaitForRechargeActivity((BatteryVehicle) vehicle, ((TaskWithStation) task).getStation());
			} else if (task instanceof RechargeTask) {
				return new RechargeActivity((BatteryVehicle) vehicle, ((TaskWithStation) task).getStation());
			}
		}
		
		return delegate.createAction(dynAgent, vehicle, now);
	}
}
