package tools.prefdumper.plugin.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import tools.prefdumper.common.ConfigurationParser;
import tools.prefdumper.plugin.conf.JolokiaCalcConfigurationParser;

public class JolokiaCalcModule implements Module {

	@Override public void configure(Binder binder) {
		Multibinder<ConfigurationParser> mb = Multibinder.newSetBinder(binder, ConfigurationParser.class);
		mb.addBinding().to(JolokiaCalcConfigurationParser.class);
	}
}
