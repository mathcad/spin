package org.spin.core.util.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.AsyncUtils;
import org.spin.core.util.ImageUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.MapUtils;
import org.spin.core.util.XmlUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.awt.*;
import java.awt.image.BufferedImage;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    private String loginInfo = "<v:Envelope xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\"><v:Header /><v:Body><n0:loginByPhone id=\"o0\" c:root=\"1\" xmlns:n0=\"http://wse.oa.srt.com/\"><phone i:type=\"d:string\">13955363928</phone><password i:type=\"d:string\">08d9f467677cef1663a5ca112ecd4e76</password></n0:loginByPhone></v:Body></v:Envelope>";

    @Test
    void testLogin() {
        String result = Http.POST.withUrl("http://47.106.86.168:7071/welcome/cxf/login?wsdl").withXmlBody(loginInfo).execute();
        XmlUtils xml = new XmlUtils(result);
        String node = xml.getLastNodesbyXPath("//return").getText();
        MrxResult<Map<String, Object>> obj = JsonUtils.fromJson(node, new TypeToken<MrxResult<Map<String, Object>>>() {
        });
        System.out.println(obj.data);

        System.out.println(obj.data.get("token"));
    }

    @Test
    void testCardRecord() throws IOException {
        procImage("D:\\cat.jpg");
    }

    @Test
    void procMask() throws IOException {
        BufferedImage mask = readMask();
        int[] rgb = new int[3];
        int width = mask.getWidth();
        int height = mask.getHeight();
        int minX = mask.getMinX();
        int minY = mask.getMinY();
        for (int y = minY; y < height; y++) {
            for (int x = minX; x < width; x++) {
                int pixel = mask.getRGB(x, y);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                if (rgb[0] < 5 || rgb[1] < 5 || rgb[2] < 5) {
                    mask.setRGB(x, y, 0);
                }
            }
        }
        ImageIO.write(mask, "BMP", new File("D:\\mask_n.bmp"));
    }

    void procImage(String imagePath) throws IOException {
        BufferedImage bi = ImageUtils.scale(ImageIO.read(new File(imagePath)), 1080, 1458, false, null);
        BufferedImage mask = readMask();

        // 叠加水印
        int[] rgb = new int[3];
        int width = mask.getWidth();
        int height = mask.getHeight();
        int minX = mask.getMinX();
        int minY = mask.getMinY();
        int dif = bi.getHeight() - mask.getHeight();
        for (int y = minY; y < height; y++) {
            for (int x = minX; x < width; x++) {
                int pixel = mask.getRGB(x, y);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                if (height - y > 30) {
                    int back = bi.getRGB(x, y + dif);
                    int alpha = back & 0xff000000;
                    rgb[0] = Math.min(((back & 0xff0000) >> 16) + rgb[0], 255);
                    rgb[1] = Math.min(((back & 0xff00) >> 8) + rgb[1], 255);
                    rgb[2] = Math.min(((back & 0xff)) + rgb[2], 255);

                    back = alpha | ((rgb[0]) << 16) | ((rgb[1] << 8)) | rgb[2];
                    bi.setRGB(x, y + dif, back);
                } else {
                    bi.setRGB(x, y + dif, pixel);
                }
            }
        }

        try (InputStream aixing = new FileInputStream(new File("D:\\MILT_RG.ttf"))) {
            Font dynamicFont = Font.createFont(Font.TRUETYPE_FONT, aixing);
            // 绘制时间
            ImageUtils.pressText(bi, "安徽", dynamicFont, Font.BOLD, Color.WHITE, 38, 10, 89, 1);
            ImageUtils.pressText(bi, "2019-12-19", dynamicFont, Font.BOLD, Color.WHITE, 34, 10, 136, 1);
            ImageUtils.pressText(bi, "11:02", dynamicFont, Font.BOLD, Color.WHITE, 34, 10, 185, 1);
        } catch (FontFormatException e) {
            e.printStackTrace();
        }

        ImageWriter writer = null;
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(bi);
        Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "jpg");
        if (iter.hasNext()) {
            writer = iter.next();
        }
        if (writer == null) {
            return;
        }
        IIOImage iioImage = new IIOImage(bi, null, null);
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(1);
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(new File("D:\\test.jpg"));
        writer.setOutput(outputStream);
        writer.write(null, iioImage, param);

//        ImageIO.write(bi, "JPEG", new File("D:\\test.jpg"));
    }


    BufferedImage readMask() {
        try {
            return ImageIO.read(new File("D:\\mask_bottom.bmp"));
        } catch (IOException e) {
            throw new SimplifiedException(e);
        }
    }

    public static class MrxResult<T> {
        private T data;
        private String msg;
        private Integer status;

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
