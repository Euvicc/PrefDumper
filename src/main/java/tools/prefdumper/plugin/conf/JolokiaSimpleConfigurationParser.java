package tools.prefdumper.plugin.conf;

import static com.google.common.base.Preconditions.*;
import static tools.prefdumper.common.helper.ParseHelper.*;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.google.inject.Singleton;

import tools.prefdumper.common.ConfigurationParser;
import tools.prefdumper.plugin.conf.JolokiaSimpleConfiguration.JolokiaSimpleConfigurationBuilder;

/**
 * 設定ファイルからインスタンスを生成するためのクラス。
 * @author imaik26
 *
 */
@Singleton
public class JolokiaSimpleConfigurationParser implements ConfigurationParser {
	private static final String TYPE = "JolokiaSimple";

	@Override
	public JolokiaSimpleConfiguration parse(Object json) throws Throwable {
		JSONObject root = (JSONObject) json;

		// 戻り値がnullの場合は対象の設定が該当しないと判断するため、初期値をnullとする。
		JolokiaSimpleConfiguration conf = null;
		JolokiaSimpleConfigurationBuilder builder = JolokiaSimpleConfiguration.builder();

		if (isOldVersion(root)) {
			if (isJolokiaSimpleConf(root)) {
				builder.key(UUID.randomUUID().toString());
				builder.requestUrl(checkNotNullString(root, "requestUrl"));
				builder.interval(Long.valueOf(checkNotNullString(root, "interval")));

				List<String> mbeans = perseMbeanPath(checkNotNullString(root, "readPath"));
				Iterator<String> ite = mbeans.iterator();
				builder.mbeanPath(ite.next());
				if (mbeans.size() > 2) {
					builder.mbeanAttribute(ite.next());
				}
				builder.mbeanValue(ite.next());
				builder.hostname(getTargetHostName(checkNotNullString(root, "requestUrl")));

				conf = builder.build();
			}
		} else {
			if (isThisConf(root)) {
				// 該当する場合
				JSONObject settings = checkNotNull((JSONObject) root.get("settings"));
				builder.key(checkNotNullString(settings, "key"));
				builder.requestUrl(checkNotNullString(settings, "requestUrl"));
				builder.mbeanPath(checkNotNullString(settings, "mbeanPath"));
				if (settings.containsKey("mbeanAttribute")) {
					builder.mbeanAttribute(checkNotNullString(settings, "mbeanAttribute"));
				}
				builder.mbeanValue(checkNotNullString(settings, "mbeanValue"));
				builder.interval(Long.valueOf(checkNotNullString(settings, "interval")));
				builder.hostname(getTargetHostName(checkNotNullString(settings, "requestUrl")));

				conf = builder.build();
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
	private boolean isJolokiaSimpleConf(JSONObject root) {
		return ! root.containsKey("calc");
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
