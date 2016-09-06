package tools.prefdumper;

import java.io.File;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.prefdumper.common.Configuration;
import tools.prefdumper.common.ConfigurationParser;
import tools.prefdumper.common.TaskExecutor;

/**
 * アプリケーションの起動クラス
 *
 * @author imaik26
 */
@Slf4j
public class Application implements DaemonRunner {

	/** 停止しているかどうか */
	@Getter private boolean stopped = Boolean.FALSE;
	/** このメソッドを呼ぶと、停止可能な状態になった場合、停止処理を実施する。 */
	@Override public void stop() { this.stopped = Boolean.TRUE; }
	/** service 起動用の初期化処理メソッド */
	@Override
	public void init(String[] args) {
		// タスク実行用エグゼキュータの作成
		this.executorService = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * パーサクラスセット。
	 * <p>
	 * {@code resources/META-INF/service/com.google.inject.Module}
	 * で設定しているModuleクラスで パーサに依存注入をする。
	 *
	 * <p>
	 * {@link tools.perfdumper.plugin.conf.JolokiaSimpleConfigurationParser
	 * JolokiaSimpleConfigurationParser}は
	 * {@link tools.perfdumper.plugin.module.JolokiaSimpleModule
	 * JolokiaSimpleModule}に記載。
	 * <p>
	 * 新しくパーサーを追加したい場合は、必要なクラスを作成したうえで、Moduleクラスを
	 * {@code resources/META-INF/service/com.google.inject.Module}に追記すれば、このセットに
	 * 追加される。
	 *
	 * @see <a href=
	 *      "http://blog.satotaichi.info/simple-plugin-system-on-guice/">
	 *      Guiceで簡易的なプラグインシステムを構築するには</a>
	 */
	@Inject
	private Set<ConfigurationParser> parsers;

	private ScheduledExecutorService executorService = null;

	/**
	 * 直接起動用起動ポイント
	 *
	 * @param args
	 *            引数（未使用）
	 */
	public static void main(String[] args) {
		Application app = newInstance();
		app.init(args);
		app.run();
	}

	/**
	 * 自身のインスタンスを生成する。
	 * <p>
	 * GuiceでDIを行うためModuleをロードしているが、動的ロードをしたいがために、 ServiceLoaderを用いている。
	 *
	 * @see <a href=
	 *      "http://blog.satotaichi.info/simple-plugin-system-on-guice/">
	 *      Guiceで簡易的なプラグインシステムを構築するには</a>
	 * @return Applicationインスタンス
	 */
	public static Application newInstance() {
		ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
		return Guice.createInjector(loader).getInstance(Application.class);
	}

	/**
	 * 実行の開始メソッド
	 */
	@Override
	public void run() {
		log.info("start pref dumper!");
		try {
			TimeUnit intervalUnit = TimeUnit.SECONDS;

			// 設定ファイルを展開し、設定ごとの配列にする。
			String filePath = System.getProperty("tools.prefdumper.conf.path", ClassLoader.getSystemResource("conf-dev.json").getFile());
			String configuration = Files.toString(new File(filePath), Charsets.UTF_8);
			JSONArray nodes = (JSONArray) new JSONParser().parse(configuration);
			log.info("設定の読み込み。");

			// 設定ファイルをパースして、適切なconfigurationクラスに変換する。
			Set<Configuration> confList = mapmConfigurations(nodes, parsers);
			log.info("パース処理。");

			// configurationクラスからタスクを生成し、スケジュールの設定を行う。
			for (Configuration conf : confList) {
				log.debug("{}\tconf:{}", conf.getClass(), conf.toString());
				this.executorService.scheduleAtFixedRate(
						new TaskExecutor(conf.getTask()), 0, conf.getInterval(), intervalUnit);
			}

			// 終了処理が行われるまで待機する。
			while (!isStopped()) {
				log.debug("{}", "running pref dumper!");

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
					break;
				}
			}

		} catch (Throwable e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			term();
		}
	}

	/**
	 * Jsonリストをパースクラスセットの中でパースできたものをパースした形で抽出する。<br />
	 * <br />
	 * パースすることができないJsonNodeはワーニングログを吐いてスキップする。<br />
	 * 複数のパースができる場合は、最初にパースしたパーサーの変換結果で返却される。<br />
	 *
	 * @param nodes
	 *            Jsonノードのリスト
	 * @param parsers
	 *            パースクラスセット
	 * @return パース後のセット
	 */
	private Set<Configuration> mapmConfigurations(JSONArray nodes, Set<? extends ConfigurationParser> parsers)
			throws Throwable {
		Set<Configuration> confList = new HashSet<Configuration>();
		for (Object node : nodes) {
			Configuration conf = null;
			for (ConfigurationParser parser : parsers) {
				conf = parser.parse(node);
				if (conf != null) {
					break;
				}
			}

			if (conf == null) {
				log.warn("設定が読み込めません。jsonNode :{}", node);
			} else {
				confList.add(conf);
			}
		}

		if (confList.size() == 0) {
			throw new Exception("パースエラー。パースできた設定が一つもありません。");
		}

		return confList;
	}

	/**
	 * 終了処理
	 */
	private void term() {
		this.stop();
		this.executorService.shutdown();
	}
}
