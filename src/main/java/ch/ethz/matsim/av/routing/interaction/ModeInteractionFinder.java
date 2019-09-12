package ch.ethz.matsim.av.routing.interaction;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.facilities.Facility;

public class ModeInteractionFinder implements AVInteractionFinder {
	private final Network network;

	public ModeInteractionFinder(Network network) {
		this.network = network;
	}

	@Override
	public Facility findPickupFacility(Facility fromFacility, double departureTime) {
		return findFacility(fromFacility);
	}

	@Override
	public Facility findDropoffFacility(Facility toFacility, double departureTime) {
		return findFacility(toFacility);
	}

	private Facility findFacility(Facility baseFacility) {
		if (baseFacility.getCoord() == null) {
			throw new IllegalStateException("Trying to find closest interaction facility, but not coords are given.");
		}
		
		return new LinkWrapperFacility(NetworkUtils.getNearestLink(network, baseFacility.getCoord()));
	}
}
