package ch.ethz.matsim.av.framework;

import java.net.URL;

import org.matsim.core.config.ReflectiveConfigGroup;

public class AVConfigGroup extends ReflectiveConfigGroup {
	static final public String GROUP_NAME = "av";
	static final public String CONFIG = "config";
	static final public String PARALLEL_ROUTERS = "parallelRouters";
	static final public String ACCESS_EGRESS_TYPE = "accessEgressType";
	static final public String ACCESS_EGRESS_LINK_FLAG = "accessEgressLinkFlag";

	private String configPath;
	private URL configURL;

	private long parallelRouters = 4;

	public enum AccessEgressType {
		NONE, MODE, ATTRIBUTE
	}

	private AccessEgressType accessEgressType = AccessEgressType.NONE;
	private String accessEgressLinkFlag = "avAccessEgress";

	public AVConfigGroup() {
		super(GROUP_NAME);
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

	@StringGetter(ACCESS_EGRESS_TYPE)
	public AccessEgressType getAccessEgressType() {
		return accessEgressType;
	}

	@StringSetter(ACCESS_EGRESS_TYPE)
	public void setAccessEgressType(AccessEgressType accessEgressType) {
		this.accessEgressType = accessEgressType;
	}
	
	@StringGetter(ACCESS_EGRESS_LINK_FLAG)
	public String getAccessEgressLinkFlag() {
		return accessEgressLinkFlag;
	}

	@StringSetter(ACCESS_EGRESS_LINK_FLAG)
	public void setAccessEgressLinkFlag(String accessEgressLinkFlag) {
		this.accessEgressLinkFlag = accessEgressLinkFlag;
	}
}
