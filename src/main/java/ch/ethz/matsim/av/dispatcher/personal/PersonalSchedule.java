package ch.ethz.matsim.av.dispatcher.personal;

import org.matsim.api.core.v01.population.Person;

import ch.ethz.matsim.av.dispatcher.trip_schedule.TripSchedule;

public class PersonalSchedule extends TripSchedule<PersonalTrip> {
	final private Person person;
	
	public PersonalSchedule(Person person) {
		super();
		this.person = person;
	}

	public Person getPerson() {
		return person;
	}
}
