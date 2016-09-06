package tools.prefdumper.common.helper;

import static com.google.common.base.Preconditions.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MalformedObjectNameException;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONObject;

import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class ParseHelper {

	public static String getTargetHostName(String requestUrl) {
		if (isLocal(requestUrl)) {
			return getLocalHostName();
		} else {
			return readJolokiaHostName(requestUrl);
		}
	}

	public static boolean isLocal(String requestUrl) {
		return requestUrl.indexOf("localhost") != -1;
	}

	public static String getLocalHostName() {
		String hostname = null;

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (Throwable e) {
			log.warn("ホスト名取得エラー。nullで続行します。", e);
		}

		return hostname;
	}

	/**
	 * ホスト名の取得
	 *
	 * @param urlRoot
	 *            jolokiaアクセスURLのルート
	 * @return ホスト名 ※ 接続できなかった場合nullを返す。
	 */
	public static String readJolokiaHostName(String requestUrl) {
			String hostname = null;
			try {
				J4pReadRequest req = new J4pReadRequest("java.lang:type=Runtime", "Name");
				String tmp = readJolokiaValue(requestUrl, req);
				hostname = tmp.substring(tmp.indexOf("@") + 1);

				// ホスト名にドメインがついている場合があるためそれを取り去る
				int index = hostname.indexOf(".");
				if (index != -1) {
					hostname = hostname.substring(0, index);
				}
			} catch (Throwable e) {
				log.warn("ホスト名取得エラー。nullで続行します。", e);
			}

			return hostname;
	}

	public static String readJolokiaValue(String urlRoot, J4pReadRequest req)
			throws MalformedObjectNameException, J4pException {
		J4pClient j4pClient = new J4pClient(urlRoot);
		J4pReadResponse resp = j4pClient.execute(req);
		return resp.getValue();
	}

	/**
	 * readPath のパース. "/"で区切るが、"!/"の場合はエスケープとする。
	 * 渡される、J4pReadRequestがエスケープ不要であるため、!/ => /
	 *
	 * @param readPath
	 */
	public static List<String> perseMbeanPath(String readPath) {
		if (readPath == null) {
			return null;
		}

		List<String> tokens = new ArrayList<String>(Arrays.asList(readPath.split("/")));

		int before = 0;
		int i = 0;
		while (i != tokens.size()) {
			if (i <= 0
					|| tokens.get(before).charAt(
							tokens.get(before).length() - 1) != '!') {
				before = i;
				i += 1;
				continue;
			}
			tokens.set(before, tokens.get(before).substring(0, tokens.get(before).length() - 1) + "/" + tokens.get(i));
			tokens.remove(i);

		}
		return tokens;
	}

	public static String checkNotNullString(Object obj, String key) throws Throwable {
		return checkNotNull(((JSONObject) obj).get(key), key + "が未設定です。").toString();
	}

}
