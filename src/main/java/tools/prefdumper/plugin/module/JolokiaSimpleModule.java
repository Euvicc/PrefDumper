package tools.prefdumper.plugin.module;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import tools.prefdumper.common.ConfigurationParser;
import tools.prefdumper.plugin.conf.JolokiaSimpleConfigurationParser;

/**
 * {@link tools.prefdumper.plugin.conf.JolokiaSimpleConfigurationParser JolokiaSimpleConfigurationParser} の依存注入用モジュールクラス
 * @author imaik26
 *
 */
public class JolokiaSimpleModule implements Module {

	@Override public void configure(Binder binder) {
		Multibinder<ConfigurationParser> mb = Multibinder.newSetBinder(binder, ConfigurationParser.class);
		mb.addBinding().to(JolokiaSimpleConfigurationParser.class);
	}
}
