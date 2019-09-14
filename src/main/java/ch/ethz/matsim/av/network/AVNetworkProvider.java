package ch.ethz.matsim.av.network;

import java.util.HashSet;
import java.util.Set;

import org.jboss.logging.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.filter.NetworkFilterManager;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.InteractionFinderConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.config.operator.WaitingTimeConfig;
import ch.ethz.matsim.av.data.AVOperator;

public class AVNetworkProvider {
	private final static Logger logger = Logger.getLogger(AVNetworkProvider.class);

	private final String allowedLinkMode;
	private final String allowedLinkAttribute;
	private final AVConfigGroup config;

	public AVNetworkProvider(String allowedLinkMode, String allowedLinkAttribute, AVConfigGroup config) {
		this.allowedLinkAttribute = allowedLinkAttribute;
		this.allowedLinkMode = allowedLinkMode;
		this.config = config;
	}

	public Network apply(Id<AVOperator> operatorId, Network fullNetwork, AVNetworkFilter customFilter) {
		NetworkFilterManager manager = new NetworkFilterManager(fullNetwork);

		if (allowedLinkMode != null) {
			manager.addLinkFilter(l -> {
				return l.getAllowedModes().contains(allowedLinkMode);
			});
		}

		if (allowedLinkAttribute != null) {
			manager.addLinkFilter(l -> {
				Boolean attribute = (Boolean) l.getAttributes().getAttribute(allowedLinkAttribute);
				return attribute != null && attribute;
			});
		}

		manager.addLinkFilter(l -> {
			return customFilter.isAllowed(operatorId, l);
		});

		Network filteredNetwork = manager.applyFilters();

		int numberOfLinks = filteredNetwork.getLinks().size();
		int numberOfNodes = filteredNetwork.getNodes().size();

		new NetworkCleaner().run(filteredNetwork);

		int cleanedNumberOfLinks = filteredNetwork.getLinks().size();
		int cleanedNumberOfNodes = filteredNetwork.getNodes().size();

		if (numberOfLinks != cleanedNumberOfLinks || numberOfNodes != cleanedNumberOfNodes) {
			logger.error(String.format("Links before/after cleaning: %d/%d", numberOfLinks, cleanedNumberOfLinks));
			logger.error(String.format("Links before/after cleaning: %d/%d", numberOfNodes, cleanedNumberOfNodes));
			throw new IllegalStateException(String.format(
					"The current network definition (mode and attribute) for operator %s is not valid!", operatorId));
		}

		if (numberOfLinks == 0) {
			throw new IllegalStateException(String.format(
					"The current network definition (mode and attribute) for operator %s is empty!", operatorId));
		}

		// <Hack> to make code compatible with MATSim 11, where network attributes were
		// not copied to the cleaned/filtered/etc networks.

		Set<String> attributes = new HashSet<>();

		for (OperatorConfig operatorConfig : config.getOperatorConfigs().values()) {
			InteractionFinderConfig interactionConfig = operatorConfig.getInteractionFinderConfig();
			attributes.add(interactionConfig.getParams().get("allowedLinkAttribute"));
			
			WaitingTimeConfig waitingTimeConfig = operatorConfig.getWaitingTimeConfig();
			attributes.add(waitingTimeConfig.getConstantWaitingTimeLinkAttribute());
			attributes.add(waitingTimeConfig.getEstimationLinkAttribute());
		}

		for (Link link : filteredNetwork.getLinks().values()) {
			for (String attribute : attributes) {
				if (attribute != null) {
					Object value = fullNetwork.getLinks().get(link.getId()).getAttributes().getAttribute(attribute);

					if (value != null) {
						link.getAttributes().putAttribute(attribute, value);
					}
				}
			}
		}

		// </Hack>
		
		return filteredNetwork;
	}
}
