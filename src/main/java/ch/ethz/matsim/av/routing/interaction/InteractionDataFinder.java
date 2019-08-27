package ch.ethz.matsim.av.routing.interaction;

import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.facilities.Facility;

public class InteractionDataFinder implements AVInteractionFinder {
	private final InteractionLinkData data;

	public InteractionDataFinder(InteractionLinkData data) {
		this.data = data;
	}

	@Override
	public Facility findPickupFacility(Facility fromFacility, double departureTime) {
		return new LinkWrapperFacility(data.getClosestLink(fromFacility.getCoord()));
	}

	@Override
	public Facility findDropoffFacility(Facility toFacility, double departureTime) {
		return findPickupFacility(toFacility, departureTime);
	}
}
