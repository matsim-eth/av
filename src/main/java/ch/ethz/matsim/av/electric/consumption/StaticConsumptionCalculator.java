package ch.ethz.matsim.av.electric.consumption;

import org.matsim.contrib.dvrp.path.VrpPath;

public class StaticConsumptionCalculator implements ConsumptionCalculator {
	final private double energyByDistance;
	final private double energyByDuration;
	
	public StaticConsumptionCalculator(double energyByDistance, double energyByDuration) {
		this.energyByDistance = energyByDistance;
		this.energyByDuration = energyByDuration;
	}

	@Override
	public double calculateConsumptionForDuration(double startTime, double endTime) {
		return energyByDuration * (endTime - startTime);
	}

	@Override
	public double calculateConsumptionForPath(double startTime, double endTime, VrpPath path) {
		return energyByDistance * calculateDistance(path);
	}
	
	private double calculateDistance(VrpPath path) {
		double distance = 0.0;
		
		for (int i = 0; i < path.getLinkCount(); i++) {
			distance += path.getLink(i).getLength();
		}
		
		return distance;
	}

	@Override
	public double calculateConsumptionForDistance(double startTime, double endTime, double distance) {
		return energyByDistance * distance;
	}
}
