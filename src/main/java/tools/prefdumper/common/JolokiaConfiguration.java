package tools.prefdumper.common;

import org.jolokia.client.request.J4pType;

public interface JolokiaConfiguration {

	public String getRequestUrl();

	public J4pType getRequestType();

	public String getMbeanPath();

	public String getMbeanAttribute();

	public String getMbeanValue();

}
