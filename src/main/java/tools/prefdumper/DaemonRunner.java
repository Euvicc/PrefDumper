package tools.prefdumper;

/**
 * procrunを用いたLauncherクラスから呼ばれることができる
 *
 * @author imaik26
 */
public interface DaemonRunner extends Runnable {

	/** このメソッドが呼ばれた場合、速やかに終了処理を実施する。 */
	public void stop();

	/** 既に終了処理が開始されているかどうか */
	public boolean isStopped();

	/** Launcherから起動される際に初期化を行う。*/
	public void init(String[] args);

}
