package ch.ethz.matsim.av.data;

import ch.ethz.matsim.av.config.AVOperatorConfig;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;

@Singleton
public class AVOperatorFactory {
    public AVOperator createOperator(Id<AVOperator> id, AVOperatorConfig config) {
        return new AVOperatorImpl(id, config);
    }
}
