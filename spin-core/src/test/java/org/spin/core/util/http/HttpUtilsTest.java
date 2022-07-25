package org.spin.core.util.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.spin.core.collection.Pair;
import org.spin.core.collection.Tuple;
import org.spin.core.concurrent.Async;
import org.spin.core.function.serializable.Function;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.*;
import org.spin.core.util.file.FileType;

import javax.imageio.ImageIO;
import javax.net.ssl.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
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

    //    @Test
    public void test1() {
        String url = "http://192.168.12.54:9200/mall_goods/_analyze";
        Map<String, Object> param = MapUtils.ofMap("field", "ware_name", "text", "华为手机");
        String s = Http.GET.withUrl(url)
            .withHead("Authorization", "Basic bWFsbDoxMjM0NTY=")
            .withJsonBody(param)
            .execute();
        System.out.println(s);
    }

    //    @Test
    public static void testM() throws InterruptedException {
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
            Async.run(() -> {
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
            Async.run(() -> {
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
    }

    //    @Test
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
            Http.configure().withKeyStore(certInput, "1530112491", KeyStoreType.PKCS12, null).finishConfigure();
        } catch (IOException e) {
        }
//        String execute = Http.POST.withUrl("https://api.mch.weixin.qq.com/secapi/pay/refund").timeout(60000).connTimeout(2000).withXmlBody("<xml><refundFee>10</refundFee><nonce_str>1561452473</nonce_str><out_trade_no>20190621135913442141</out_trade_no><totalFee>10</totalFee><outTradeNo>20190621135913442141</outTradeNo><appid>wxfa3f617e8c84dc09</appid><total_fee>10</total_fee><refund_fee>10</refund_fee><sign>2F118B65923E01D425C2E26E3601A444</sign><mch_id>1530112491</mch_id></xml>").execute();
//        System.out.println(execute);
        Http.configure().withHostnameVerifier((ss, c) -> true).finishConfigure();
        Http.GET.withUrl("https://cn.bing.com/search?q=httpclient+%E5%A4%9A%E4%B8%AA%E8%AF%81%E4%B9%A6&qs=n&form=QBLH&sp=-1&pq=httpclient+%E5%A4%9A%E4%B8%AAvg&sc=0-15&sk=&cvid=06539FD6052F4DFEABAE2CDD0BB60938")
            .executeAsyncAsString(System.out::println);
        long s = System.currentTimeMillis();
        Future<Map<String, Object>> future = Http.POST.withUrl("http://192.168.40.151/bonade-uaac/v1/auth").withJsonBody("{\n" +
                "    \"loginType\": \"NORMAL\",\n" +
                "    \"loginName\": \"aRC1jtdyPqj9CZNSdusyLB40OCinV9+//5ZIui+e1M7kKC4/wis2jvwT6Z2JhzM1oFFZ3woESpnQRmzuhwvxXTsCi/JrJd5wA2ZtATD++zqjfss19CtKTWcmiZNI4KmFmpdkESeBVONeNyY22dIDgNvOTHpm/YMX5lLk6X8gyGk=\",\n" +
                "    \"secret\": \"TR9FSK4Ut5li5AmBbmxD69KaRFO3SXwho6E5NYKyV8F2w6zxE32wF3r2MLB6AB8pqY9r7SWsqTYOWlaCDrmnx6cde+60E44Foim+F942irgGzyV3FMIewm2rhbl8aqEtU5Q72eX6LAzGX/1zYuLqXllWe5VpeWOL2woP9x0amto=\",\n" +
                "    \"rememberMe\": false,\n" +
                "    \"clientType\": \"WEB\",\n" +
                "    \"target\": \"MODULE:VISITOR-ENT-ADMIN\",\n" +
                "    \"webSocketId\": \"0e2131ee-9aca-4206-8e49-e24621bfdb21\"\n" +
                "}")
            .executeAsync(JsonUtils.MAP_TYPE_TOKEN, e -> {
                throw new SpinException(e);
            });
        System.out.println("aaaaaa:" + (System.currentTimeMillis() - s));
//        future.cancel(true);
        Map<String, Object> res = future.get();
        System.out.println("aaaaaa:" + (System.currentTimeMillis() - s));
        System.out.println(res);

//        execute = Http.GET.withUrl("https://cn.bing.com/search?q=httpclient+%E5%A4%9A%E4%B8%AA%E8%AF%81%E4%B9%A6&qs=n&form=QBLH&sp=-1&pq=httpclient+%E5%A4%9A%E4%B8%AAvg&sc=0-15&sk=&cvid=06539FD6052F4DFEABAE2CDD0BB60938").execute();

//        System.out.println(execute);
    }


    @Test
    void testConvert() {
        ObjectUtils.convert(List.class, new ArrayList<>());
    }

}
