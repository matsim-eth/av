package ch.ethz.matsim.av.data;

import org.matsim.api.core.v01.Id;
import ch.ethz.matsim.av.config.AVOperatorConfig;

public interface AVOperator {
    Id<AVOperator> getId();
    AVOperatorConfig getConfig();
}
