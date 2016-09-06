package tools.prefdumper.common;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

/**
 * Taskインタフェースを実装した値取得タスククラスを実行して、ログ出力を行うクラス。
 * @author imaik26
 *
 */
@Slf4j
public class TaskExecutor implements Runnable {

	private Task task;

	public TaskExecutor(Task task) {
		log.info("new task : {}", task.getClass());
		this.task = task;
	}

	/**
	 * 実行可能なデータ取得タスクを実行し、その結果をMDCを用いてログに登録する。
	 */
	@Override
	public void run() {
		SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		sdf.applyPattern("yyyy/MM/dd:HH:mm:ss");

		Result result = task.run();

		try {
			MDC.clear();
			MDC.put("key", result.getKey());
			MDC.put("time", sdf.format(result.getAcquisitionTime()));
			MDC.put("hostname", result.getAcquisitionHostname());
			MDC.put("value", result.getValue());
			Map<String, ?> attributes = result.getOtherAttribute();
			for (String key : attributes.keySet()) {
				MDC.put(key, attributes.get(key).toString());
			}

			log.info("print result");
		} finally {
			MDC.clear();
		}
	}

}
