package tools.prefdumper.plugin.conf;

import static org.junit.Assert.*;

import java.net.InetAddress;

import org.json.simple.parser.JSONParser;
import org.junit.Test;

import tools.prefdumper.plugin.conf.JolokiaSimpleConfiguration.JolokiaSimpleConfigurationBuilder;

public class JolokiaSimpleConfigurationParserTest {

	JolokiaSimpleConfigurationParser target = new JolokiaSimpleConfigurationParser();

	@Test
	public void testParse_属性未指定シンプルJSONチェック() throws Throwable {
		String key = "test";
		String requestUrl = "http://localhost/jolokia";
		String mbeanPath = "mbea\\\"nPath";
		String mbeanValue = "mbeanValue";
		String hostname = InetAddress.getLocalHost().getHostName();
		long interval = 60L;

		JolokiaSimpleConfigurationBuilder builder = JolokiaSimpleConfiguration.builder();
		builder.key(key).requestUrl(requestUrl).mbeanPath(mbeanPath.replace("\\\"", "\"")).mbeanValue(mbeanValue).hostname(hostname).interval(interval);

		String json = ""
						+ "	{"
						+ "		\"confType\" : \"JolokiaSimple\","
						+ "		\"settings\" : {"
						+ "			\"requestUrl\" : \"" + requestUrl + "\","
						+ "			\"mbeanPath\" : \"" + mbeanPath + "\","
						+ "			\"mbeanValue\" : \"" + mbeanValue + "\","
						+ "			\"key\" : \"" + key + "\","
						+ "			\"interval\" : " + interval
						+ "		}"
						+ "	}";
		JSONParser parser = new JSONParser();

		JolokiaSimpleConfiguration conf = target.parse(parser.parse(json));

		assertEquals(conf.toString(), builder.build().toString());
	}

	@Test
	public void testParse_属性指定シンプルJSONチェック() throws Throwable {
		String key = "test";
		String requestUrl = "http://localhost/jolokia";
		String mbeanPath = "mbeanPath";
		String mbeanAttribute = "mbeanAttribute";
		String mbeanValue = "mbeanValue";
		String hostname = InetAddress.getLocalHost().getHostName();
		long interval = 60L;

		JolokiaSimpleConfigurationBuilder builder = JolokiaSimpleConfiguration.builder();
		builder.key(key).requestUrl(requestUrl).mbeanPath(mbeanPath).mbeanAttribute(mbeanAttribute)
			.mbeanValue(mbeanValue).hostname(hostname).interval(interval);

		String json = ""
				+ "	{"
				+ "		\"confType\" : \"JolokiaSimple\","
				+ "		\"settings\" : {"
				+ "			\"requestUrl\" : \"" + requestUrl + "\","
				+ "			\"mbeanPath\" : \"" + mbeanPath + "\","
				+ "			\"mbeanAttribute\" : \"" + mbeanAttribute + "\","
				+ "			\"mbeanValue\" : \"" + mbeanValue + "\","
				+ "			\"key\" : \"" + key + "\","
				+ "			\"interval\" : " + interval
				+ "		}"
				+ "	}";
		JSONParser parser = new JSONParser();

		JolokiaSimpleConfiguration conf = target.parse(parser.parse(json));

		assertEquals(conf.toString(), builder.build().toString());
	}

	@Test
	public void testParse_該当しない場合() throws Throwable {
		String json = ""
			+ "	{"
			+ "		\"confType\" : \"JolokiaSimpleA\","
			+ "		\"settings\" : {"
			+ "			\"requestUrl\" : \"http://localhost/jolokia\","
			+ "			\"mbeanPath\" : \"mbeanPath\","
			+ "			\"mbeanAttribute\" : \"mbeanAttribute\","
			+ "			\"mbeanValue\" : \"mbeanValue\","
			+ "			\"key\" : \"test\","
			+ "			\"interval\" : 60"
			+ "		}"
			+ "	}";
		JSONParser parser = new JSONParser();

		JolokiaSimpleConfiguration conf = target.parse(parser.parse(json));

		assertNull(conf);
	}

	@Test(expected = NullPointerException.class)
	public void testParse_フィールドが不足している場合() throws Throwable {
		String json = ""
				+ "	{"
				+ "		\"confType\" : \"JolokiaSimple\","
				+ "		\"settings\" : {"
				+ "			\"mbeanPath\" : \"mbeanPath\","
				+ "			\"mbeanAttribute\" : \"mbeanAttribute\","
				+ "			\"mbeanValue\" : \"mbeanValue\","
				+ "			\"key\" : \"test\","
				+ "			\"interval\" : 60"
				+ "		}"
				+ "	}";
		JSONParser parser = new JSONParser();

		JolokiaSimpleConfiguration conf = target.parse(parser.parse(json));

		assertNull(conf);
	}

	@Test
	public void testParse_属性指定シンプルJSONチェック_旧バージョン() throws Throwable {
		// セットアップ
		String requestUrl = "http://localhost:8778/jolokia";
		// 「"」のエスケープ「\"」が入ってきた場合に、「"」として認識するか確認
		String mbeanPath = "java.lang:type=Mem\\\"ory";
		// 「!」で「/」をエスケープした場合に、正しく分割されるか、また、「!」が削除されるか確認。
		String mbeanAttribute = "HeapMemory!/Usage";
		String mbeanValue = "used";
		String key = "test";
		long interval = 5;
		String json = ""
						+ "	{"
						+ "		\"requestUrl\" : \"" + requestUrl + "\","
						+ "		\"readPath\" : \"" + mbeanPath + "/" + mbeanAttribute + "/" + mbeanValue + "\","
						+ "		\"interval\" : " + interval
						+ "	}";

		JolokiaSimpleConfigurationBuilder builder = JolokiaSimpleConfiguration.builder();
		builder.requestUrl(requestUrl).mbeanPath(mbeanPath.replace("\\\"", "\"")).mbeanAttribute(mbeanAttribute.replace("!", "")).mbeanValue(mbeanValue)
			.interval(interval).key(key);

		JSONParser parser = new JSONParser();
		JolokiaSimpleConfiguration conf = target.parse(parser.parse(json));

		assertNotNull(conf.getKey());
		conf.key = key;
		assertNotNull(conf.getHostname());
		conf.hostname = null;
		assertEquals(conf.toString(), builder.build().toString());
	}
}
