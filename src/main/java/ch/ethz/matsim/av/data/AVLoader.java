package ch.ethz.matsim.av.data;

import ch.ethz.matsim.av.schedule.AVStayTask;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

@Singleton
public class AVLoader implements BeforeMobsimListener {
    @Inject
    private AVData data;

    @Override
    public void notifyBeforeMobsim(BeforeMobsimEvent event) {
        for (Vehicle vehicle : data.getVehicles().values()) {
            vehicle.resetSchedule();

            Schedule schedule = vehicle.getSchedule();
            schedule.addTask(new AVStayTask(vehicle.getServiceBeginTime(), vehicle.getServiceEndTime(), vehicle.getStartLink()));
        }
    }
}
