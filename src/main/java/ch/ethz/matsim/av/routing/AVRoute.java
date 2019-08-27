package ch.ethz.matsim.av.routing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.population.routes.AbstractRoute;
import org.matsim.facilities.Facility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import ch.ethz.matsim.av.data.AVOperator;

public class AVRoute extends AbstractRoute {
	final static String AV_ROUTE = "av";

	private Id<AVOperator> operatorId;
	private Id<Facility> accessFacilityId;
	private Id<Facility> egressFacilityId;

	public AVRoute(Id<Link> startLinkId, Id<Link> endLinkId) {
		super(startLinkId, endLinkId);
	}

	public Id<AVOperator> getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Id<AVOperator> operatorId) {
		this.operatorId = operatorId;
	}

	public Id<Facility> getAccessFacilityId() {
		return accessFacilityId;
	}

	public void setAccessFacilityId(Id<Facility> accessFacilityId) {
		this.accessFacilityId = accessFacilityId;
	}

	public Id<Facility> getEgressFacilityId() {
		return egressFacilityId;
	}

	public void setEgressFacility(Id<Facility> egressFacilityId) {
		this.egressFacilityId = egressFacilityId;
	}

	private void interpretAttributes(Map<String, String> attributes) {
		String operatorId = attributes.get("operatorId");
		String accessFacilityId = attributes.get("accessFacilityId");
		String egressFacilityId = attributes.get("egressFacilityId");

		this.operatorId = Id.create(operatorId, AVOperator.class);
		this.accessFacilityId = Id.create(accessFacilityId, Facility.class);
		this.egressFacilityId = Id.create(egressFacilityId, Facility.class);
	}

	private Map<String, String> buildAttributes() {
		Map<String, String> attributes = new HashMap<>();
		attributes.put("operatorId", operatorId.toString());
		attributes.put("accessFacilityId", accessFacilityId.toString());
		attributes.put("egressFacilityId", egressFacilityId.toString());
		return attributes;
	}

	private final ObjectMapper mapper = new ObjectMapper();
	private final MapType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class,
			String.class);

	@Override
	public String getRouteDescription() {
		try {
			return new ObjectMapper().writeValueAsString(buildAttributes());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setRouteDescription(String routeDescription) {
		try {
			Map<String, String> attributes = mapper.readValue(routeDescription, mapType);
			interpretAttributes(attributes);
		} catch (IOException e) {
			new RuntimeException(e);
		}
	}

	@Override
	public String getRouteType() {
		return AV_ROUTE;
	}
}
