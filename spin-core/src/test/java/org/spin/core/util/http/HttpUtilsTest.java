package org.spin.core.util.http;

import org.apache.http.HttpEntity;
import org.junit.jupiter.api.Test;
import org.spin.core.util.AsyncUtils;
import org.spin.core.util.MapUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

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
    public void testHttps() throws FileNotFoundException {
        try (InputStream certInput = new FileInputStream(new File("C:\\Users\\Mathcat\\Desktop\\apiclient_cert.p12"))) {
            Http.initSync(certInput, "1530112491", "PKCS12");
        } catch (IOException e) {
        }
        String execute = Http.POST.withUrl("https://api.mch.weixin.qq.com/secapi/pay/refund").withXmlBody("<xml><refundFee>10</refundFee><nonce_str>1561452473</nonce_str><out_trade_no>20190621135913442141</out_trade_no><totalFee>10</totalFee><outTradeNo>20190621135913442141</outTradeNo><appid>wxfa3f617e8c84dc09</appid><total_fee>10</total_fee><refund_fee>10</refund_fee><sign>2F118B65923E01D425C2E26E3601A444</sign><mch_id>1530112491</mch_id></xml>").execute();

        System.out.println(execute);
        execute = Http.GET.withUrl("https://cn.bing.com/search?q=httpclient+%E5%A4%9A%E4%B8%AA%E8%AF%81%E4%B9%A6&qs=n&form=QBLH&sp=-1&pq=httpclient+%E5%A4%9A%E4%B8%AAvg&sc=0-15&sk=&cvid=06539FD6052F4DFEABAE2CDD0BB60938").execute();
        System.out.println(execute);
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
}
