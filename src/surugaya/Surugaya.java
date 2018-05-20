package surugaya;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 駿河屋スクライピング用クラス
 * @author horad
 *
 */
public class Surugaya {
	private boolean debug = true;
	/**
	 *駿河屋 URi
	 */
	private String baseUri = "https://www.suruga-ya.jp/";

	public static void main(String args[]) {

		//argsにて検索モード

	}

	/**
	 * カテゴリごとに検索ワードにてヒットした検索結果を表示する。<p>
	 * searchCategoryNoは以下のように割り振られている<p>
	 * 例外：以下のカテゴリNo以外を入れるとNullpointerException発生<p>
	 * 0 全商品<p>
	 * (1 全商品)←1に割り当てはないが、全商品として検索される<p>
	 * 2 ゲーム<p>
	 * 3 映像ソフト<p>
	 * 4 音楽ソフト<p>
	 * 5 おもちゃ・ホビー<p>
	 * 6 PCソフト<p>
	 * 7 本<p>
	 * 8 電気製品<p>
	 * 9 食品・食玩<p>
	 * 10 雑貨・小物<p>
	 * 11 同人<p>
	 * @param searchCategoryNo カテゴリNo.
	 * @param searchString 検索ワード
	 * @return list 検索にヒットしなれば list.size() = 0
	 */
	public ArrayList<String[]> searchCategory(int searchCategoryNo, String searchString) {
		ArrayList<String[]> buf = new ArrayList<String[]>();//return search result
		Document document = null;//html documnet

		if ((searchCategoryNo < 0) || (searchCategoryNo > 11)) {//異常処理
			System.out.println("error: arg error");
			return null;
		}

		try {
			document = Jsoup.connect(baseUri + "search?category=" + searchCategoryNo
								+ "&search_word=" + searchString).get();//get document
		} catch (IOException e) {//異常処理
			// TODO 自動生成された catch ブロック
			System.out.println("error:can not connect uri");
			return null;
		}

		Elements els = document.select(".item");
		for(Element el : els) {
			buf.add(getItemDetail(el));

		}

		if(debug) {
			for(String s[] :buf) {
				IntStream.rangeClosed(0, s.length-1).forEach(i -> System.out.println(s[i]));
				System.out.println();
			}

		}

		return buf;
	}

	/**
	 * searchCategoryの配列戻り値版
	 * @param searchCategoryNo カテゴリNo.
	 * @param searchString 検索ワード
	 * @return list 検索にヒットしなれば list.size() = 0
	 */
	public String[][] searchCategoryAsArray(int searchCategoryNo, String searchString) {
		ArrayList<String[]> buf = searchCategory(searchCategoryNo, searchString);//return search result

		return buf.toArray(new String[buf.size()][buf.get(0).length]);

	}



	/**
	 * @param el 検索エレメント
	 * @return URI,condition,title,price
	 */
	private String[] getItemDetail(Element el) {
		String tmp[] = new String[4];

		tmp[0] = el.select("a").attr("href");//uri
		tmp[1] = el.select(".condition span").text().replace(el.select(".sale").text(), "").trim();//condition
		tmp[2] = el.select(".title").text();//title
		tmp[3] = el.select(".price").text().replace(" 税込", "").replace("￥", "").replace(",", "");//price

//		if(debug) {
//			IntStream.rangeClosed(0, tmp.length-1).forEach(i -> System.out.println(tmp[i]));
//		}

		return tmp;
	}

	/**
	 * top menuのカテゴリ取得関数。作りかけ。用途不明なため、そのうち消すかもしれない。
	 */
	public void getCategryList() {
		String tmp[] = new String[2];
		Document document = null;//html documnet
		try {
			document = Jsoup.connect(baseUri).get();//get document
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			System.out.println("error:can not connect uri");
		}

		//get elements(class=border_bottom)
		Elements els = document.select(".border_bottom li");

		for (Element el : els) {
			tmp[0] = el.select("a").attr("href");
			tmp[1] = el.text();

			if (debug) {
				System.out.println("href: " + tmp[0]);
				System.out.println("CategryList: " + tmp[1]);
			}
		}

	}

	/**
	 * 駿河屋商品ページから品名、商品状態、価格、商品コードを取得する。<p>
	 * uri example:https://www.suruga-ya.jp/product/detail/(商品コード)<p>
	 * list(0):品名<p>
	 * list(1):商品状態<p>
	 * list(2):価格<p>
	 * list(3):商品コード<p>
	 * @param Uri 検索URI
	 * @return ArrayList&lt;String&gt; list<p>
	 * 			検索するURIがなければnull
	 */
	private ArrayList<String> getProductDetail(String Uri) {
		ArrayList<String> buf = new ArrayList<String>();//return value)title/state/price/proid
		Document document = null;//html documnet
		String tmp = "";
		try {
			document = Jsoup.connect(Uri).get();//get document
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			System.out.println("error:can not connect uri");
			return null;
		}

		//get title
		tmp = document.select("h2").text().replace(document.select("h2").select("span").text(), "").trim();
		buf.add(tmp);
		if (debug)
			System.out.println("title: " + buf.get(0));

		//get state and price
		try {
			tmp = document.getElementById("price").text();//tmp price
			if (tmp.contains("新品")) {
				buf.add("Brandnew");
				buf.add(tmp.replace("新品販売価格", "")
						.replace("円 (税込)", "")
						.replace(",", ""));
			} else if (tmp.contains("中古")) {
				buf.add("Secondhand");
				buf.add(tmp.replace("中古販売価格", "")
						.replace("円 (税込)", "")
						.replace(",", ""));
			} else if (tmp.contains("予約")) {//品切れ
				buf.add("Reservation");
				buf.add(tmp.replace("予約販売価格", "")
						.replace("円 (税込)", "")
						.replace(",", ""));
			}
		} catch (NullPointerException e) {//品切れ
			buf.add("Outofstock");
			buf.add(null);
		}

		if (debug) {
			System.out.println("state: " + buf.get(1));
			System.out.println("price: " + buf.get(2));
		}

		//get item proid
		buf.add(document.getElementById("proid").text());
		if (debug)
			System.out.println("proid: " + buf.get(3));

		return buf;
	}
}
