package tools.prefdumper.common;

public interface Configuration {

	/**
	 * 定期的に実行される取得処理クラスを取得する。
	 *
	 * @return TaskCreator スケジューリングタスク
	 */
	public Task getTask();

	/**
	 * タスクを実行する頻度(秒)を返す。
	 */
	public long getInterval();

	/**
	 * 設定の名称。設定ごとに生成される一意のキー名。
	 * @return key
	 */
	public String getKey();

	/**
	 * 取得対象サーバのホスト名
	 * @return Stirng ホスト名
	 */
	public String getHostname();

	/**
	 * CURLコマンドを生成するクラスを返す。
	 * @return CurlCreatable
	 */
	public CurlCreatable getCurlCreatable();

	/**
	 * CURLコマンドを生成することができるかどうか。
	 * @return
	 */
	public boolean canCurlCreate();
}
