package tools.prefdumper.plugin.task;

import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.exception.J4pRemoteException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;

import lombok.extern.slf4j.Slf4j;
import tools.prefdumper.common.Result;
import tools.prefdumper.common.Result.ResultBuilder;
import tools.prefdumper.common.Task;
import tools.prefdumper.plugin.conf.JolokiaSimpleConfiguration;

@Slf4j
public class JolokiaSimpleTask implements Task {

	/** タスクを実行するもとになる設定ファイル */
	private JolokiaSimpleConfiguration conf;

	/** Jolokiaのリクエストオブジェクト */
	private J4pReadRequest req = null;

	/** 取得結果を格納するResultオブジェクトのビルダー */
	private ResultBuilder builder = null;

	/** コンストラクタ */
	public JolokiaSimpleTask(JolokiaSimpleConfiguration conf) {
		this.conf = conf;
	}

	/**
	 * JolokiaClient用のリクエストオブジェクトを生成する。
	 * また、遅延実行とする。
	 *
	 * @return リクエストオブジェクト
	 * @throws MalformedObjectNameException
	 */
	private J4pReadRequest createRequest() throws MalformedObjectNameException {
		if (this.req == null) {
			J4pReadRequest req = new J4pReadRequest(conf.getMbeanPath(), conf.getMbeanAttribute());
//			if (conf.getMbeanAttribute() != null) {
//				req.setPath(conf.getMbeanAttribute());
//			}
			req.setPath(conf.getMbeanValue());
			this.req = req;
		}
		return this.req;
	}

	/**
	 * 取得結果のビルダーを作る。
	 * <p>初回だけ生成し、次回以降は流用する。
	 * <p>毎回異なる値が設定されるべき項目は上書きして使う想定
	 * @return
	 */
	private ResultBuilder createBuilder() {
		if (this.builder == null) {
			ResultBuilder builder = Result.builder();
			builder.acquisitionHostname(conf.getHostname());
			builder.key(conf.getKey());

			Map<String, String> attribute = new HashMap<String, String>();
			attribute.put("mbeanPath", conf.getMbeanPath());
			if (conf.getMbeanAttribute() != null) {
				attribute.put("mbeanAttribute", conf.getMbeanAttribute());
			}
			attribute.put("mbeanValue", conf.getMbeanValue());
			attribute.put("requestUrl", conf.getRequestUrl());

			builder.otherAttribute(attribute);
			this.builder = builder;
		}

		return this.builder;
	}

	@Override
	public Result run() {
		ResultBuilder builder = createBuilder();
		try {
			J4pReadResponse resp = new J4pClient(conf.getRequestUrl()).execute(createRequest());
			builder.acquisitionTime(resp.getRequestDate());
			builder.value(resp.getValue().toString());

		} catch (J4pRemoteException e) {
			log.error("リクエストが正しくありません。{}", conf.getCurlCreatable().createCrulCommands());
		} catch (MalformedObjectNameException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (J4pException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (Throwable e) {
			log.error(e.getLocalizedMessage(), e);
		}

		return builder.build();
	}

}
