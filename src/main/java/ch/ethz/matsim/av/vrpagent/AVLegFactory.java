package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.vrpagent.VrpLeg;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.core.mobsim.framework.MobsimTimer;

import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.schedule.AVOptimizer;

public class AVLegFactory implements VrpLegFactory {
	final private MobsimTimer mobsimTimer;
	final private AVOptimizer optimizer;

	public AVLegFactory(MobsimTimer mobsimTimer, AVOptimizer optimizer) {
		this.mobsimTimer = mobsimTimer;
		this.optimizer = optimizer;
	}

	@Override
	public VrpLeg create(Vehicle vehicle) {
		return VrpLegFactory.createWithOnlineTracker(AVModule.AV_MODE, vehicle, optimizer, mobsimTimer);
	}
}
