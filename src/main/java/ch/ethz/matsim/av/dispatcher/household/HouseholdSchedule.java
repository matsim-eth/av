package ch.ethz.matsim.av.dispatcher.household;

import org.matsim.households.Household;

import ch.ethz.matsim.av.dispatcher.trip_schedule.TripSchedule;

public class HouseholdSchedule extends TripSchedule<HouseholdTrip> {
	final private Household household;
	
	public HouseholdSchedule(Household household) {
		super();
		this.household = household;
	}

	public Household getHousehold() {
		return household;
	}
}
