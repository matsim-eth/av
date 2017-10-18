package ch.ethz.matsim.av.electric.assets.battery;

public class DefaultBattery implements Battery {
	final private BatterySpecification specification;
	private double state;
	
	public DefaultBattery(BatterySpecification specification) {
		this.specification = specification;
	}
	
	@Override
	public BatterySpecification getSpecification() {
		return specification;
	}

	@Override
	public double getState() {
		return state;
	}

	@Override
	public void setState(double state) {
		this.state = state;
	}
	
}
