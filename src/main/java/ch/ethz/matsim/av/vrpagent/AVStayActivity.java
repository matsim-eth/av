package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dynagent.AbstractDynActivity;

import ch.ethz.matsim.av.schedule.AVStayTask;

public class AVStayActivity extends AbstractDynActivity {
	final private AVStayTask stayTask;
	private double now;
	
	public AVStayActivity(AVStayTask stayTask) {
		super(stayTask.getName());
		this.stayTask = stayTask;
		this.now = stayTask.getBeginTime();
	}
	
	@Override
	public void doSimStep(double now) {
		this.now = now;
	}

	@Override
	public double getEndTime() {
		if (Double.isInfinite(stayTask.getEndTime())) {
			return now + 1;
		} else {
			return stayTask.getEndTime();
		}
	}

}
