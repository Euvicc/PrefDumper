package tools.prefdumper.plugin.conf;

import static com.google.common.base.Preconditions.*;
import static tools.prefdumper.common.helper.ParseHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.inject.Singleton;

import tools.prefdumper.common.Configuration;
import tools.prefdumper.common.ConfigurationParser;
import tools.prefdumper.plugin.conf.JolokiaCalcConfiguration.CalcType;
import tools.prefdumper.plugin.conf.JolokiaCalcConfiguration.JolokiaCalcConfigurationBuilder;
import tools.prefdumper.plugin.conf.JolokiaCalcConfiguration.JolokiaConfigurationImpl;

@Singleton
public class JolokiaCalcConfigurationParser implements ConfigurationParser {
	private static final String TYPE = "JolokiaCalc";

	@Override
	public Configuration parse(Object json) throws Throwable {
		JSONObject root = (JSONObject) json;

		// 戻り値がnullの場合は対象の設定が該当しないと判断するため、初期値をnullとする。
		JolokiaCalcConfiguration conf = null;
		JolokiaCalcConfigurationBuilder builder = JolokiaCalcConfiguration.builder();

		if (isOldVersion(root)) {
			if (isJolokiaCalcConf(root)) {
				builder.key(UUID.randomUUID().toString());
				builder.requestUrl(checkNotNullString(root, "requestUrl"));
				builder.interval(Long.valueOf(checkNotNullString(root, "interval")));
				builder.hostname(getTargetHostName(checkNotNullString(root, "requestUrl")));
				builder.calc(CalcType.valueOf(checkNotNullString(root, "calc")));

				conf = builder.build();

				List<JolokiaConfigurationImpl> requestConfs = new ArrayList<JolokiaConfigurationImpl>();
				for (Object mbean : checkNotNull((JSONArray) root.get("readPath"))) {
					List<String> mbeans = perseMbeanPath(mbean.toString());
					JolokiaConfigurationImpl requestConf = null;
					if (mbeans.size() > 2) {
						requestConf = conf.new JolokiaConfigurationImpl(
								mbeans.get(0),
								mbeans.get(1),
								mbeans.get(2));
					} else {
						requestConf = conf.new JolokiaConfigurationImpl(
								mbeans.get(0),
								mbeans.get(1));
					}
					requestConfs.add(requestConf);
				}
				conf.requestConfs = requestConfs;
			}
		} else {
			if (isThisConf(root)) {
				// 該当する場合
				JSONObject settings = checkNotNull((JSONObject) root.get("settings"));
				builder.key(checkNotNullString(settings, "key"));
				builder.requestUrl(checkNotNullString(settings, "requestUrl"));
				builder.interval(Long.valueOf(checkNotNullString(settings, "interval")));
				builder.hostname(getTargetHostName(checkNotNullString(settings, "requestUrl")));
				builder.calc(CalcType.valueOf(checkNotNullString(settings, "calc")));

				conf = builder.build();

				List<JolokiaConfigurationImpl> requestConfs = new ArrayList<JolokiaConfigurationImpl>();
				for (Object mbean : checkNotNull((JSONArray) settings.get("mbean"))) {
					JolokiaConfigurationImpl requestConf = null;
					if (((JSONObject) mbean).containsKey("mbeanAttribute")) {
						requestConf = conf.new JolokiaConfigurationImpl(
								checkNotNullString(mbean, "mbeanPath"),
								checkNotNullString(mbean, "mbeanAttribute"),
								checkNotNullString(mbean, "mbeanValue"));
					} else {
						requestConf = conf.new JolokiaConfigurationImpl(
								checkNotNullString(mbean, "mbeanPath"),
								checkNotNullString(mbean, "mbeanValue"));
					}
					requestConfs.add(requestConf);
				}
				conf.requestConfs = requestConfs;
			}
		}

		return conf;
	}

	/**
	 * 旧バージョンの設定ファイルかどうか
	 * @param root 設定ファイルのルートオブジェクト
	 * @return
	 */
	private boolean isOldVersion(JSONObject root) {
		return ! root.containsKey("settings");
	}

	/**
	 * 旧バージョンのJolokiaシンプルコンフィグかどうか
	 * @param root 設定ファイルのルートオブジェクト
	 * @return
	 */
	private boolean isJolokiaCalcConf(JSONObject root) {
		return root.containsKey("calc");
	}

	/**
	 * 該当設定が生成対象の設定かどうか
	 * @param root 設定ファイルのルートオブジェクト
	 * @return
	 */
	private boolean isThisConf(JSONObject root) {
		return root.containsKey("confType") && root.get("confType").equals(TYPE);
	}
}
