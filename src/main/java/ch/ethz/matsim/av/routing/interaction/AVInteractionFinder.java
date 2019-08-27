package ch.ethz.matsim.av.routing.interaction;

import org.matsim.facilities.Facility;

public interface AVInteractionFinder {
	Facility findPickupFacility(Facility fromFacility, double departureTime);

	Facility findDropoffFacility(Facility toFacility, double departureTime);
}
