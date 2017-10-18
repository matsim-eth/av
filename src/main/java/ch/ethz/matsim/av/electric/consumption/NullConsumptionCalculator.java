package ch.ethz.matsim.av.electric.consumption;

import org.matsim.contrib.dvrp.path.VrpPath;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NullConsumptionCalculator implements ConsumptionCalculator {
	@Inject
	public NullConsumptionCalculator() {}
	
	@Override
	public double calculateConsumptionForDuration(double startTime, double endTime) {
		return 0;
	}

	@Override
	public double calculateConsumptionForPath(double startTime, double endTime, VrpPath path) {
		return 0;
	}

	@Override
	public double calculateConsumptionForDistance(double startTime, double endTime, double distance) {
		return 0;
	}

}
