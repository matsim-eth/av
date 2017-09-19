package ch.ethz.matsim.av.generator;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.dynagent.run.DynActivityEngine;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;

import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.schedule.AVOptimizer;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.vrpagent.AVActionCreator;

public class AVVehicleCreator {
	final private AVOptimizer optimizer;
	final private QSim qsim;
	final private AVActionCreator actionCreator;
	final private DynActivityEngine activityEngine;

	public AVVehicleCreator(QSim qsim, AVOptimizer optimizer, AVActionCreator actionCreator,
			DynActivityEngine activityEngine) {
		this.qsim = qsim;
		this.optimizer = optimizer;
		this.actionCreator = actionCreator;
		this.activityEngine = activityEngine;
	}

	public DynAgent createVehicle(AVVehicle vehicle) {
		return createVehicle(vehicle, VehicleUtils.getDefaultVehicleType());
	}

	public DynAgent createVehicle(AVVehicle vehicle, VehicleType vehicleType) {
		VehiclesFactory vehicleFactory = VehicleUtils.getFactory();

		Id<Vehicle> id = vehicle.getId();
		Id<Link> startLinkId = vehicle.getStartLink().getId();

		VrpAgentLogic vrpAgentLogic = new VrpAgentLogic(optimizer, actionCreator, vehicle);
		DynAgent vrpAgent = new DynAgent(Id.createPersonId(id), startLinkId, qsim.getEventsManager(), vrpAgentLogic);
		QVehicle mobsimVehicle = new QVehicle(
				vehicleFactory.createVehicle(Id.create(id, org.matsim.vehicles.Vehicle.class), vehicleType));
		vrpAgent.setVehicle(mobsimVehicle);
		mobsimVehicle.setDriver(vrpAgent);

		qsim.addParkedVehicle(mobsimVehicle, startLinkId);
		qsim.insertAgentIntoMobsim(vrpAgent);

		vehicle.resetSchedule();

        Schedule schedule = vehicle.getSchedule();
        schedule.addTask(new AVStayTask(vehicle.getServiceBeginTime(), vehicle.getServiceEndTime(), vehicle.getStartLink()));
        
        activityEngine.handleActivity(vrpAgent);

        return vrpAgent;
	}
}
