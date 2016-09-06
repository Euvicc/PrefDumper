package tools.prefdumper.plugin.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;

import lombok.extern.slf4j.Slf4j;
import tools.prefdumper.common.Result;
import tools.prefdumper.common.Result.ResultBuilder;
import tools.prefdumper.common.Task;
import tools.prefdumper.plugin.conf.JolokiaCalcConfiguration;
import tools.prefdumper.plugin.conf.JolokiaCalcConfiguration.JolokiaConfigurationImpl;

@Slf4j
public class JolokiaCalcTask implements Task {

	/** タスクを実行するもとになる設定ファイル */
	private JolokiaCalcConfiguration conf;

	/** Jolokiaのリクエストオブジェクト */
	private List<J4pReadRequest> reqs = null;

	/** 取得結果を格納するResultオブジェクトのビルダー */
	private ResultBuilder builder = null;

	/** コンストラクタ */
	public JolokiaCalcTask(JolokiaCalcConfiguration conf) {
		this.conf = conf;
	}

	/**
	 * JolokiaClient用のリクエストオブジェクトを生成する。
	 * また、遅延実行とする。
	 *
	 * @return リクエストオブジェクト
	 * @throws MalformedObjectNameException
	 */
	private List<J4pReadRequest> createRequests() throws MalformedObjectNameException {
		if (this.reqs == null) {
			List<J4pReadRequest> reqs = new ArrayList<J4pReadRequest>();
			for (JolokiaConfigurationImpl jcon : conf.getRequestConfs()) {
				J4pReadRequest req = new J4pReadRequest(jcon.getMbeanPath(),
						jcon.getMbeanValue());
				if (jcon.getMbeanAttribute() != null) {
					req.setPath(jcon.getMbeanAttribute());
				}
				reqs.add(req);
			}
			this.reqs = reqs;
		}
		return this.reqs;
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
			List<Integer> values = new ArrayList<Integer>();
			Iterator<J4pReadRequest> i1 = createRequests().iterator();
			Date requestDate = null;
			do {
				J4pReadRequest req = i1.next();
				J4pReadResponse resp = new J4pClient(conf.getRequestUrl()).execute(req);
				requestDate = resp.getRequestDate();
				values.add(Integer.valueOf(resp.getValue().toString()));
			} while(i1.hasNext());

			Iterator<Integer> i2 = values.iterator();
			Integer value = i2.next();
			while (i2.hasNext()) {
				switch (conf.getCalc()) {
					case MINUS:
						value -= i2.next();
						break;
					case PLUS:
					default:
						value += i2.next();
						break;
				}
			}

			builder.acquisitionTime(requestDate);
			builder.value(value.toString());

		} catch (MalformedObjectNameException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (J4pException e) {
			log.error(e.getLocalizedMessage(), e);
		}

		return builder.build();
	}

}
