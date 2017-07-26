package ch.ethz.matsim.av.framework;

import org.matsim.core.config.ReflectiveConfigGroup;

import java.net.URL;

public class AVConfigGroup extends ReflectiveConfigGroup {
	final static String AV = "av";
	final static String CONFIG = "config";
	final static String PARALLEL_ROUTERS = "parallelRouters";

	private String configPath;
	private URL configURL;

	private long parallelRouters = 4;
	
	public AVConfigGroup() {
		super(AV);
	}

	@StringGetter(CONFIG)
	public String getConfigPath() {
		return configPath;
	}

	@StringSetter(CONFIG)
	public void setConfigPath(String path) {
		configPath = path;
	}

	@StringGetter(PARALLEL_ROUTERS)
	public long getParallelRouters() {
		return parallelRouters;
	}

	@StringSetter(PARALLEL_ROUTERS)
	public void setParallelRouters(long parallelRouters) {
		this.parallelRouters = parallelRouters;
	}

	public URL getConfigURL() {
		return configURL;
	}

	public void setConfigURL(URL url) {
		this.configURL = url;
	}
}
