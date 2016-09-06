package tools.prefdumper;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * procrun用の起動用クラス
 *
 * @author imaik26
 */
@Slf4j
public class DaemonLauncher implements Daemon {

	private static DaemonRunner daemon = null;
	private static DaemonLauncher daemonLauncherInstance = new DaemonLauncher();
	private ExecutorService executor = null;

	private enum COMMAND {
		START("start"),
		STOP("stop"),
		;
		@Getter
		private String command;
		private COMMAND(String string) {command = string;}
		public String toString() {return command;}
	}

	/**
	 * 起動ポイント
	 * Windowsサービスの場合は使用しない。
	 * この起動ポイントはコンソールで起動した場合にサービス起動/停止と同様の操作ができる。
	 *
	 * @param args 引数
	 */
	public static void main(String[] args) {

		daemonLauncherInstance.startWindowsService();

		Scanner sc = null;
		try {
			sc = new Scanner(System.in);
			System.out.println("Enter '" + COMMAND.STOP.toString() + "' to halt: ");

			while (!sc.nextLine().toLowerCase().equals(COMMAND.STOP.toString())) {
				;
			}
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
		daemonLauncherInstance.stopWindowsService();
	}

	/**
	 * 初期化処理
	 * Windows/Linuxどちらでも使う.
	 */
	private void initialize() {
		if (daemon == null) {
			log.info("Starting the Daemon");
			daemon = Application.newInstance();
		}
		executor = Executors.newSingleThreadExecutor();
	}

	/**
	 * 終了処理
	 * Windows/Linuxどちらでも使う.
	 */
	private void terminate() {
		if (daemon != null) {
			log.info("Stopping the Daemon");
			daemon.stop();
			log.info("Daemon stopped");
		}
	}

	/* Windows系service用 ******************************************************/

	/**
	 * Windowsサービスを起動します。
	 * サービス登録するときのパラメータは次のようです。
	 *
	 * <pre>
	 * --StartMode=jvm \
	 * --StartClass=tools.prefdumper.DaemonLauncher \
	 * --StartMethod=windowsService \
	 * --StartParams=start \
	 * --StopMode=jvm \
	 * --StopClass=tools.prefdumper.DaemonLauncher \
	 * --StopMethod=windowsService \
	 * --StopParams=stop
	 * </pre>
	 *
	 * Windowsサービス、つまりprocrunで起動する場合、実行するメソッドは"public static void"である必要があります。
	 * voidでなくてもいいかもしれませんが、戻り値を解釈しないので、voidがいいと思います。
	 *
	 * @param args
	 *            Arguments from prunsrv command line
	 **/
	public static void windowsService(String args[]) {
		String cmd = COMMAND.START.toString();
		if (args.length > 0) {
			cmd = args[0];
		}

		if (COMMAND.START.toString().equals(cmd)) {
			daemonLauncherInstance.startWindowsService();
		} else {
			daemonLauncherInstance.stopWindowsService();
		}
	}

	/**
	 * Windowsサービスの場合のスタートメソッド。
	 *
	 * 新しいスレッドを起動し、本アプリを実行する。
	 */
	public void startWindowsService() {
		log.info("startWindowsService called");

		initialize();

/*
		// もともとコマンドライン起動を想定していたため、コンフィグファイルのパスを
		// コマンドラインオプションで受け取る作りになっている。
		// Serviceからも起動できるように、JVMプロパティから取得して、クッションとする。
		String[] args = {"-c", System.getProperty("config.file")};
		daemon.init(args);
*/

		// don't return until stopped
		executor.execute(daemon);
	}

	/**
	 * Windowsサービスの場合のストップメソッド。
	 *
	 * 終了処理をして、スレッドの終了を行う。
	 */
	public void stopWindowsService() {
		log.info("stopWindowsService called");

		terminate();

		executor.shutdown();
	}

	/* Linux系service用 ********************************************************/
	/* - 現在未実装                                                            */

	/**
	 * Linux系サービスの場合の初期化処理メソッド
	 * 現在未実装
	 */
	@Override
	@Deprecated
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		log.debug("Daemon init");
	}

	/**
	 * Linux系サービスの場合のスタートメソッド
	 * 現在未実装
	 */
	@Override
	@Deprecated
	public void start() throws Exception {
		log.debug("Daemon start");

		initialize();
	}

	/**
	 * Linux系サービスの場合のストップメソッド
	 * 現在未実装
	 */
	@Override
	@Deprecated
	public void stop() throws Exception {
		log.debug("Daemon stop");

		terminate();
	}

	/**
	 * Linux系サービスの場合の終了処理メソッド
	 * 現在未実装
	 */
	@Override
	@Deprecated
	public void destroy() {
		log.debug("Daemon destroy");
	}

}
