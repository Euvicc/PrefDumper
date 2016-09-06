package tools.prefdumper.plugin.conf;

import org.jolokia.client.request.J4pType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import tools.prefdumper.common.Configuration;
import tools.prefdumper.common.CurlCreatable;
import tools.prefdumper.common.JolokiaConfiguration;
import tools.prefdumper.common.Task;
import tools.prefdumper.common.helper.JolokiaCurlCommandCreateHelper;
import tools.prefdumper.plugin.task.JolokiaSimpleTask;

/**
 * Jolokiaを叩くための一番シンプルな設定。
 * @author imaik26
 *
 */
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Builder
public class JolokiaSimpleConfiguration implements Configuration, JolokiaConfiguration {

	protected long interval = 60L;

	@NonNull
	protected String requestUrl;

	@NonNull
	protected String mbeanPath;

	protected String mbeanAttribute;

	@NonNull
	protected String mbeanValue;

	protected String hostname;

	@NonNull
	protected String key;

	@Override
	public Task getTask() {
		return new JolokiaSimpleTask(this);
	}

	@Override
	public J4pType getRequestType() {
		return J4pType.READ;
	}

	@Override
	public CurlCreatable getCurlCreatable() {
		return JolokiaCurlCommandCreateHelper.getCurlCommandCreator(this);
	}

	@Override
	public boolean canCurlCreate() {
		return true;
	}

}
