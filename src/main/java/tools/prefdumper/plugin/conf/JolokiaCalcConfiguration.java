package tools.prefdumper.plugin.conf;

import java.util.List;

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
import tools.prefdumper.plugin.task.JolokiaCalcTask;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Builder
public class JolokiaCalcConfiguration implements Configuration {

	protected long interval = 60L;

	@NonNull
	protected String requestUrl;

	protected String hostname;

	protected List<JolokiaConfigurationImpl> requestConfs;

	@NonNull
	protected String key;

	@NonNull
	protected CalcType calc;

	@Override
	public Task getTask() {
		return new JolokiaCalcTask(this);
	}

	@Override
	public CurlCreatable getCurlCreatable() {
		return JolokiaCurlCommandCreateHelper.getCurlCommandCreator(this.requestConfs);
	}

	@Override
	public boolean canCurlCreate() {
		return true;
	}

	@RequiredArgsConstructor
	@ToString
	@EqualsAndHashCode
	@AllArgsConstructor
	@Getter
	public class JolokiaConfigurationImpl implements JolokiaConfiguration {

		@Override
		public String getRequestUrl() {
			return JolokiaCalcConfiguration.this.requestUrl;
		}

		@NonNull
		protected String mbeanPath;

		protected String mbeanAttribute;

		@NonNull
		protected String mbeanValue;

		@Override
		public J4pType getRequestType() {
			return J4pType.READ;
		}
	}

	public enum CalcType {
	    // Supported:
	    PLUS("plus"),
	    MINUS("minus"),
	    ;

	    private String value;

	    CalcType(String cValue) {
	        value = cValue;
	    }

	    public String getValue() {
	        return value;
	    }
	}
}
