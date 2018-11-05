package ch.ethz.matsim.av.vrpagent;

import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVPassengerDropoffActivity;
import ch.ethz.matsim.av.passenger.AVPassengerPickupActivity;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.schedule.AVTask;
import com.google.inject.Inject;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.vrpagent.VrpActivity;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynAgent;

import javax.inject.Named;

public class AVActionCreator implements VrpAgentLogic.DynActionCreator {
    public static final String PICKUP_ACTIVITY_TYPE = "AVPickup";
    public static final String DROPOFF_ACTIVITY_TYPE = "AVDropoff";
    public static final String STAY_ACTIVITY_TYPE = "AVStay";

    @Inject @Named(AVModule.AV_MODE)
    private PassengerEngine passengerEngine;

    @Inject
    private VrpLegFactory legFactory;

    @Inject @Named("pickupDuration")
    private Double pickupDuration;

    @Override
    public DynAction createAction(DynAgent dynAgent, Vehicle vehicle, double now)
    {
		Task task = vehicle.getSchedule().getCurrentTask(); 
    	if (task instanceof AVTask) {
    		switch (((AVTask) task).getAVTaskType()) {
    			case PICKUP:
    				AVPickupTask mpt = (AVPickupTask) task;
    	    		return new AVPassengerPickupActivity(passengerEngine, dynAgent, vehicle, mpt, mpt.getRequests(),
    	                    pickupDuration, PICKUP_ACTIVITY_TYPE);
                case DROPOFF:
    				AVDropoffTask mdt = (AVDropoffTask) task;
    				return new AVPassengerDropoffActivity(passengerEngine, dynAgent, vehicle, mdt, mdt.getRequests(),
                            DROPOFF_ACTIVITY_TYPE);
                case DRIVE:
    				return legFactory.create(vehicle);
                case STAY:
                    return new AVStayActivity((AVStayTask) task);
                	//return new VrpActivity(((AVStayTask)task).getName(), (StayTask) task);
    	    	default:
    	    		throw new IllegalStateException();
    		}
    	} else {
    		throw new IllegalArgumentException();
        }
    }
}
