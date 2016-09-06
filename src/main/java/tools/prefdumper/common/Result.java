package tools.prefdumper.common;

import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@Builder
public class Result {

	/** 設定ファイルごとに個別に設定できる任意の値（どの設定の結果かを紐づけられるような値） */
	@NonNull
	private String key;

	/** 取得日（時） */
	@NonNull
	private Date acquisitionTime;

	/** 結果値 */
	@NonNull
	private String value;

	/** 取得を行ったホスト名 */
	@NonNull
	private String acquisitionHostname;

	/** 設定ごとの固有のキーワード */
	private Map<String, ?> otherAttribute;
}
