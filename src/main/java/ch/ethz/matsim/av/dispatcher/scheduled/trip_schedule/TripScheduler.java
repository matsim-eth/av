package ch.ethz.matsim.av.dispatcher.scheduled.trip_schedule;

import java.util.Collection;

import org.matsim.api.core.v01.Id;

import ch.ethz.matsim.av.data.AVOperator;

public interface TripScheduler {
	Collection<TripSchedule> getSchedules();
}
