package tools.prefdumper.common.helper;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import tools.prefdumper.common.CurlCreatable;
import tools.prefdumper.common.JolokiaConfiguration;

public class JolokiaCurlCommandCreateHelper {

	public static CurlCreatable getCurlCommandCreator(final JolokiaConfiguration conf) {
		return new CurlCreatable() {
			@Override
			public List<String> createCrulCommands() {
				return Arrays.asList(createCurlCommand(conf));
			}
		};
	}

	public static CurlCreatable getCurlCommandCreator(final List<? extends JolokiaConfiguration> confs) {
		return new CurlCreatable() {
			@Override
			public List<String> createCrulCommands() {
				return Lists.transform(confs, new Function<JolokiaConfiguration, String>() {
					@Override
					public String apply(JolokiaConfiguration input) {
						return createCurlCommand(input);
					}
				});
			}
		};
	}

	private static String createCurlCommand(JolokiaConfiguration conf) {
		return "curl -XGET " + conf.getRequestUrl() + "/" + conf.getRequestType().getValue() + "/" + conf.getMbeanPath()
			+ (conf.getMbeanAttribute() == null ? "" : "/" + conf.getMbeanAttribute()) + "/" + conf.getMbeanValue() + "?pretty";
	}
}
