package ch.ethz.matsim.av.electric.logic;

import ch.ethz.matsim.av.schedule.AVStayTask;
import org.matsim.api.core.v01.network.Link;

public class RechargingTask extends AVStayTask {
    public RechargingTask(double beginTime, double endTime, Link link) {
        super(beginTime, endTime, link, "AVRecharge");
    }

    @Override
    public AVTaskType getAVTaskType() {
        return AVTaskType.STAY;
    }
}
