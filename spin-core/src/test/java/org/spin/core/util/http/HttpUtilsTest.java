package org.spin.core.util.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.spin.core.util.AsyncUtils;
import org.spin.core.util.MapUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/8/14</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class HttpUtilsTest {

    @Test
    public void test1() {
        String url = "http://192.168.12.54:9200/mall_goods/_analyze";
        Map<String, Object> param = MapUtils.ofMap("field", "ware_name", "text", "华为手机");
        String s = Http.GET.withUrl(url)
            .withHead("Authorization", "Basic bWFsbDoxMjM0NTY=")
            .withJsonBody(param)
            .execute();
        System.out.println(s);
    }

    @Test
    public void testM() throws InterruptedException {
        String url = "http://www.baidu.com";
        System.out.println(Http.GET.withUrl(url).execute());

        long s = System.currentTimeMillis();
        for (int i = 0; i != 10; ++i) {
            Http.GET.withUrl(url).execute();
        }
        long e = System.currentTimeMillis();
        System.out.println(e - s);

        CountDownLatch latch = new CountDownLatch(10);
        List<Long> times = new ArrayList<>(10);

        for (int i = 0; i != 10; ++i) {
            AsyncUtils.runAsync(() -> {
                long si = System.currentTimeMillis();
                Http.GET.withUrl(url).execute();
                long ei = System.currentTimeMillis();
                times.add(ei - si);
                latch.countDown();
            });
        }
        latch.await();
        System.out.println(times.stream().reduce((a, b) -> a + b).orElse(0L));

        times.clear();
        CountDownLatch latch2 = new CountDownLatch(10);
        for (int i = 0; i != 10; ++i) {
            AsyncUtils.runAsync(() -> {
                long si = System.currentTimeMillis();
                Http.GET.withUrl(url).execute();
                long ei = System.currentTimeMillis();
                times.add(ei - si);
                latch2.countDown();
            });
        }
        latch2.await();
        System.out.println(times.stream().reduce((a, b) -> a + b).orElse(0L));
    }

    public void t() {
        EntityProcessor<String> a = new EntityProcessor<String>() {
            @Override
            public String process(HttpEntity entity) {
                return null;
            }
        };

        a.toString();
    }

    @Test
    public void testPost() {
        String url = "https://bizapi.jd.com/oauth2/access_token";

        Map<String, Object> param = MapUtils.ofMap("grant_type", "access_token",
            "client_id", "kJwCFVY8YLCom0bx0fPz",
            "username", "vop物流对接",
            "password", "9610f5b09b5262719c0decee0c6fb7e0",
            "timestamp", "2019-03-18 10:32:17",
            "sign", "4FCA330AF497D64387CCE88C921E5DBE");

        String res = Http.POST.withUrl(url).withForm(param).execute();
        System.out.println(res);
    }

    /**
     *
     */
    @Test
    public void testHttps() throws FileNotFoundException, ExecutionException, InterruptedException {
        try (InputStream certInput = new FileInputStream(new File("C:\\Users\\Mathcat\\Desktop\\apiclient_cert.p12"))) {
//            Http.initSync(certInput, "1530112491", "PKCS12");
            Http.configure().withCertificate(certInput, "1530112491", "PKCS12").finishConfigure();
        } catch (IOException e) {
        }
        String execute = Http.POST.withUrl("https://api.mch.weixin.qq.com/secapi/pay/refund").timeout(60000).connTimeout(2000).withXmlBody("<xml><refundFee>10</refundFee><nonce_str>1561452473</nonce_str><out_trade_no>20190621135913442141</out_trade_no><totalFee>10</totalFee><outTradeNo>20190621135913442141</outTradeNo><appid>wxfa3f617e8c84dc09</appid><total_fee>10</total_fee><refund_fee>10</refund_fee><sign>2F118B65923E01D425C2E26E3601A444</sign><mch_id>1530112491</mch_id></xml>").execute();
        System.out.println(execute);
        Future<HttpResponse> future = Http.POST.withUrl("https://api.mch.weixin.qq.com/secapi/pay/refund").withXmlBody("<xml><refundFee>10</refundFee><nonce_str>1561452473</nonce_str><out_trade_no>20190621135913442141</out_trade_no><totalFee>10</totalFee><outTradeNo>20190621135913442141</outTradeNo><appid>wxfa3f617e8c84dc09</appid><total_fee>10</total_fee><refund_fee>10</refund_fee><sign>2F118B65923E01D425C2E26E3601A444</sign><mch_id>1530112491</mch_id></xml>")
            .executeAsync(System.out::println, Throwable::printStackTrace);
        future.get();

//        execute = Http.GET.withUrl("https://cn.bing.com/search?q=httpclient+%E5%A4%9A%E4%B8%AA%E8%AF%81%E4%B9%A6&qs=n&form=QBLH&sp=-1&pq=httpclient+%E5%A4%9A%E4%B8%AAvg&sc=0-15&sk=&cvid=06539FD6052F4DFEABAE2CDD0BB60938").execute();
        Http.GET.withUrl("https://cn.bing.com/search?q=httpclient+%E5%A4%9A%E4%B8%AA%E8%AF%81%E4%B9%A6&qs=n&form=QBLH&sp=-1&pq=httpclient+%E5%A4%9A%E4%B8%AAvg&sc=0-15&sk=&cvid=06539FD6052F4DFEABAE2CDD0BB60938")
            .executeAsync(System.out::println, Throwable::printStackTrace);
//        System.out.println(execute);
    }

    @Test
    public void testRefund() throws IOException {
        String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
        String body = "<xml><refundFee>10</refundFee><nonce_str>1561452473</nonce_str><out_trade_no>20190621135913442141</out_trade_no><totalFee>10</totalFee><outTradeNo>20190621135913442141</outTradeNo><appid>wxfa3f617e8c84dc09</appid><total_fee>10</total_fee><refund_fee>10</refund_fee><sign>2F118B65923E01D425C2E26E3601A444</sign><mch_id>1530112491</mch_id></xml>";
        String certPasswd = "1530112491";
        String certPath = "C:\\Users\\Mathcat\\Desktop\\apiclient_cert.p12";

        URL u = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Connection", "close");
        connection.setRequestProperty("Content-Type", "application/xml");
        connection.setRequestProperty("Accept", "application/xml");
        connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

        try (InputStream certsInput = new FileInputStream(new File(certPath))) {

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(certsInput, certPasswd.toCharArray());

            keyManagerFactory.init(keyStore, certPasswd.toCharArray());
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
            connection.setSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
        }
        try (OutputStream out = connection.getOutputStream()) {
            out.write(body.getBytes());
        } catch (IOException e) {
        }

        int respCode = connection.getResponseCode();

        String con = null;
        try (InputStream in = respCode == HttpURLConnection.HTTP_OK ?
            connection.getInputStream() : connection.getErrorStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            con = content.toString();
        } catch (IOException e) {
        }
        System.out.println(con);
    }


    private String url = "https://m.cuiweijuxs.com";
    private Pattern pattern = Pattern.compile(".*第\\(?\\d+/(\\d+)\\)?页.*");
    //    String novelId = "0_562";
//    int titlePrefix = 10;
//    private String novelId = "0_794";
    private String novelId = "0_92";
    private int titlePrefix = 0;

//    @Test
//    void testNovel() {
//        Map<String, String> chapters = chapters(novelId, titlePrefix);
//
//        chapters.forEach((k, v) -> {
//            System.out.println(k);
//            System.out.println("\n");
//
//            StringBuilder sb = new StringBuilder();
//            int pageSize = parseContent(v, true, sb);
//            for (int i = 2; i <= pageSize; ++i) {
//                parseContent(v + "_" + i, false, sb);
//            }
//            System.out.println(sb.toString().replaceAll("\n+", "\n").replaceAll("\n[^ ]", ""));
//        });
//    }
//
//    private Map<String, String> chapters(String novelId, int titlePrefix) {
//        Map<String, String> chapterList = new LinkedHashMap<>();
//
//        int pageSize = parseChapter(novelId, 1, titlePrefix, chapterList);
//        for (int i = 2; i <= pageSize; i++) {
//            parseChapter(novelId, i, titlePrefix, chapterList);
//        }
//        return chapterList;
//    }
//
//    private int parseChapter(String novelId, int page, int titlePrefix, Map<String, String> chapterList) {
//        String html = Http.GET.withUrl(url + "/" + novelId + "/all" + (page == 1 ? "" : ("_" + page)) + ".html").execute();
//        Document document = Jsoup.parse(html);
//        int pageSize = 1;
//        if (1 == page) {
//            String text = document.getElementsByClass("page").get(1).text();
//            Matcher matcher = pattern.matcher(text);
//            if (matcher.find()) {
//                pageSize = Integer.parseInt(matcher.group(1));
//            }
//        }
//        Elements chapters = document.getElementsByClass("chapter").get(0).children();
//        chapters.stream().map(it -> it.child(0)).forEach(c -> chapterList.put(c.text().substring(titlePrefix), c.attr("href").replace(".html", "")));
//
//        return pageSize;
//    }
//
//    private int parseContent(String no, boolean parsePageSize, StringBuilder content) {
//        String html = Http.GET.withUrl(url + no + ".html").execute();
//        Document document = Jsoup.parse(html);
//
//        int pageSize = 1;
//        if (parsePageSize) {
//            String title = document.getElementsByClass("nr_title").get(0).text();
//            Matcher matcher = pattern.matcher(title);
//            if (matcher.find()) {
//                pageSize = Integer.parseInt(matcher.group(1));
//            }
//        }
//
//        Elements nr = document.getElementsByClass("nr_nr");
//
//        nr.forEach(e -> content.append(e.children().stream().map(Element::html)
//            .map(it -> it.replaceAll("&nbsp;", " ")
//                .replaceAll("<br>", "")
//                .replaceAll("--&gt;.*）", "")
//                .replaceAll("&amp;", "")
//                .replaceAll("amp;", "")
//            )
//            .reduce("", (a, b) -> a + b)));
//        return pageSize;
//    }
}
