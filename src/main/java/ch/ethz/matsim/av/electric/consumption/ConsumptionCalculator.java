package ch.ethz.matsim.av.electric.consumption;

import org.matsim.contrib.dvrp.path.VrpPath;

public interface ConsumptionCalculator {
	double calculateConsumptionForDuration(double startTime, double endTime);
	double calculateConsumptionForPath(double startTime, double endTime, VrpPath path);
	double calculateConsumptionForDistance(double startTime, double endTime, double distance);
}
