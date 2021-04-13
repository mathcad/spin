package org.spin.core.util.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.spin.core.collection.Pair;
import org.spin.core.collection.Tuple;
import org.spin.core.concurrent.Async;
import org.spin.core.function.serializable.Function;
import org.spin.core.gson.reflect.TypeToken;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.DateUtils;
import org.spin.core.util.ImageUtils;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.MapUtils;
import org.spin.core.util.StringUtils;
import org.spin.core.util.file.FileType;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
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

    //    @Test
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


    @Test
    void testAa() {
        int b = 24058;
        int e = 24111;
        String url = "http://m.yidudu1.org/archive.php?aid=%d";

        try (FileWriter os = new FileWriter(("D:\\b.log"))) {
            for (int i = b; i <= e; ++i) {
                Document document = Jsoup.parse(new URL(String.format(url, i)).openStream(), "GBK", url);
                Elements detail = document.getElementsByClass("detail");
                detail.get(0).children().stream().map(it -> StringUtils.trimToEmpty(it.text())).filter(StringUtils::isNotBlank).forEach(it -> {
                    try {
                        os.write(it);
                        os.write("\n");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });
                os.write("\n--------------------------------------------\n");
            }
        } catch (IOException ignore) {

        }

    }

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

    public static class KeyStoreBuilder extends KeyStore.Builder {
        private final Supplier<KeyStore> keyStoreSupplier;
        private final Function<String, KeyStore.ProtectionParameter> passwordFunction;

        public KeyStoreBuilder(Supplier<KeyStore> keyStoreSupplier, Function<String, KeyStore.ProtectionParameter> passwordFunction) {
            Objects.requireNonNull(keyStoreSupplier);
            Objects.requireNonNull(passwordFunction);
            this.keyStoreSupplier = keyStoreSupplier;
            this.passwordFunction = passwordFunction;
        }

        @Override
        public KeyStore getKeyStore() throws KeyStoreException {
            return keyStoreSupplier.get();
        }

        @Override
        public KeyStore.ProtectionParameter getProtectionParameter(String alias) throws KeyStoreException {
            Objects.requireNonNull(alias);
            return passwordFunction.apply(alias);
        }
    }

    //    @Test
    void testJks() throws Exception {
        String key = "c:/Users/Mathcat/test.keystore";
        KeyStore keystore = KeyStore.getInstance("JKS");
        //keystore的类型，默认是jks
        keystore.load(new FileInputStream(key), "123456".toCharArray());
        //创建jkd密钥访问库    123456是keystore密码。
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
        //asdfgh是key密码。
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());


        Map<String, KeyStore.ProtectionParameter> passwordsMap = new HashMap<>();
        passwordsMap.put("test1", new KeyStore.PasswordProtection("123456".toCharArray()));
        passwordsMap.put("testp12", new KeyStore.PasswordProtection("654321".toCharArray()));
        KeyStore.Builder builder = new KeyStoreBuilder(() -> keystore, alias -> {
            // alias is lowercased keystore alias with prefixed numbers :-/
            // parse the alias
            int firstDot = alias.indexOf('.');
            int secondDot = alias.indexOf('.', firstDot + 1);
            if ((firstDot == -1) || (secondDot == firstDot)) {
                // invalid alias
                return null;
            }
            String keyStoreAlias = alias.substring(secondDot + 1);
            return passwordsMap.get(keyStoreAlias);
        });

        kmf.init(new KeyStoreBuilderParameters(builder));
        X509ExtendedKeyManager keyManager = (X509ExtendedKeyManager) kmf.getKeyManagers()[0];

        String rsaAlias = keyManager.chooseServerAlias("RSA", null, null);
//        kmf.init(keystore, "123456".toCharArray());
        tmf.init(keystore);
        //创建管理jks密钥库的x509密钥管理器，用来管理密钥，需要key的密码
        SSLContext sslc = SSLContext.getInstance("SSLv3");
    }

    //    @Test
    void testBiSsl() {
        try (InputStream is = new FileInputStream(new File("c:/Users/Mathcat/test.keystore"))) {
            Http.configure().withKeyStore(is, "123456", KeyStoreType.JKS, MapUtils.ofMap("test1", "123456", "testp12", "654321")).finishConfigure();
            String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
            String body = "<xml><refundFee>10</refundFee><nonce_str>1561452473</nonce_str><out_trade_no>20190621135913442141</out_trade_no><totalFee>10</totalFee><outTradeNo>20190621135913442141</outTradeNo><appid>wxfa3f617e8c84dc09</appid><total_fee>10</total_fee><refund_fee>10</refund_fee><sign>2F118B65923E01D425C2E26E3601A444</sign><mch_id>1530112491</mch_id></xml>";
            String execute = Http.POST.withUrl(url).withXmlBody(body).execute();
            System.out.println(execute);
        } catch (IOException ignore) {
        }
    }


    @Test
    void testDownload() {
    }


    @Test
    void testSign() throws IOException {
        sign(2, "D:\\DiaDosNamorados.jpg");
    }

    private void sign(int type, String photo) throws IOException {
        String accessToken = getAccessToken();
        EmpInfo empInfo = queryUserEmployeeList(accessToken);
        Pair<String, String> records = queryDayRecordList(accessToken, LocalDate.now(), empInfo);
        String record = 1 == type ? records.c1 : records.c2;

        String pic;
        try (InputStream is = new FileInputStream(new File(photo))) {
            pic = uploadPic(accessToken, is);
//            sign(accessToken, pic, record, empInfo);
        } catch (IOException ignore) {
            throw new SimplifiedException("上传失败");
        }
        System.out.println(pic);
    }


    private String getAccessToken() {
        Map<String, Object> execute = Http.POST.withUrl("https://hrm.bndxqc.com/api/hrm-foundation/authorization/implicitAuthorize")
            .withUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36 Edg/86.0.622.69")
            .withJsonBody("{\"authenticationInfo\":{\"credentials\":{\"username\":\"13955363928\",\"password\":\"d8e70eb9752e2f5b1b3771ecb2cb39b5\"}},\"client_id\":\"hrm-app\"}")
            .execute(JsonUtils.MAP_TYPE_TOKEN);
        return StringUtils.toString(execute.get("access_token"));
    }

    private String getUser(String token) {
        String url = "https://hrm.bndxqc.com/api/hrm-foundation/user/v1/getUser";
        Map<String, Object> res = Http.POST.withUrl(url)
            .withHead("CHANNEL", "MOBILE")
            .withHead("HRM_ACCESS", token)
            .withUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36 Edg/86.0.622.69")
            .execute(JsonUtils.MAP_TYPE_TOKEN);
        return StringUtils.toString(res.get("userCode"));
    }


    private EmpInfo queryUserEmployeeList(String token) {
        String user = getUser(token);
        String url = "https://hrm.bndxqc.com/api/hrm-foundation/employeeInfo/queryUserEmployeeList";
        List<EmpInfo> res = Http.POST.withUrl(url)
            .withHead("CHANNEL", "MOBILE")
            .withHead("HRM_ACCESS", token)
            .withUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36 Edg/86.0.622.69")
            .withJsonBody(MapUtils.ofMap("userCode", user))
            .execute(new TypeToken<List<EmpInfo>>() {
            });
        return CollectionUtils.detect(res, r -> "COMb316088a6d5a4697b503aee0fbed9f2b".equals(r.getCompanyCode()));
    }


    private Pair<String, String> queryDayRecordList(String token, LocalDate date, EmpInfo empInfo) {
        String url = "https://hrm.bndxqc.com/api/hrm-assessment/assessmentRecord/v1.0/queryDayRecordList";
        Map<String, Object> res = Http.POST.withUrl(url)
            .withHead("reqhead", getReqHead(url, token))
            .withHead("channel", "MOBILE")
            .withHead("hrm_access", token)
            .withUserAgent("xqf-android/1.4.3 (Android 9; zh_CN)")
            .withJsonBody(MapUtils.ofMap("companyCode", empInfo.getCompanyCode(),
                "assessmentDate", DateUtils.formatDateForDay(date),
                "employeeCode", empInfo.getEmployeeCode()))
            .execute(JsonUtils.MAP_TYPE_TOKEN);
        return Tuple.of(
            BeanUtils.getFieldValue(res, "data.signRecordInfos[0].assessmentRecordCode"),
            BeanUtils.getFieldValue(res, "data.signRecordInfos[1].assessmentRecordCode")
        );
    }

    private String uploadPic(String token, InputStream headImage) throws IOException {
        String url = "https://hrm.bndxqc.com/api/hrm-foundation/file/v1/binFileUpload";
        BufferedImage scale = ImageUtils.scale(ImageIO.read(headImage), 144, 176, ImageUtils.ScaleMode.FILL, Color.black, Transparency.BITMASK);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileName = System.currentTimeMillis() + ".png";
        ImageUtils.writeImage(scale, FileType.Image.PNG, outputStream);
        Map<String, Object> res = Http.POST.withUrl(url)
            .withHead("reqhead", getReqHead(url, token))
            .withHead("channel", "MOBILE")
            .withHead("hrm_access", token)
            .withUserAgent("xqf-android/1.4.3 (Android 9; zh_CN)")
            .withForm("file", fileName, outputStream.toByteArray())
            .execute(JsonUtils.MAP_TYPE_TOKEN);
        return StringUtils.toString(res.get("wholeFileAddr"));

    }

    private static final SecureRandom random = new SecureRandom();

    private String sign(String token, String photo, String recordCode, EmpInfo empInfo) {
        String url = "https://hrm.bndxqc.com/api/hrm-assessment/assessmentSign/v1.0/sign";
        BigDecimal lat = new BigDecimal("31.332633");
        BigDecimal lng = new BigDecimal("118.361441");

        int offset = random.nextInt(6) - 3;
        lat = lat.add(BigDecimal.valueOf(offset).setScale(6, RoundingMode.HALF_UP).divide(new BigDecimal("100000"), 6, RoundingMode.HALF_UP));
        offset = random.nextInt(6) - 3;
        lng = lng.add(BigDecimal.valueOf(offset).setScale(6, RoundingMode.HALF_UP).divide(new BigDecimal("100000"), 6, RoundingMode.HALF_UP));
        return Http.POST.withUrl(url)
            .withHead("reqhead", getReqHead(url, token))
            .withHead("channel", "MOBILE")
            .withHead("hrm_access", token)
            .withUserAgent("xqf-android/1.4.3 (Android 9; zh_CN)")
            .withJsonBody(MapUtils.ofMap("isSignScope", "1",
                "deviceType", "MI 6",
                "companyCode", empInfo.getCompanyCode(),
                "signIp", "54:a7:03:b2:0d:c8",
                "photoUrl", photo,
                "signAddress", "安徽省芜湖市镜湖区长江中路3号靠近中国农业银行(芜湖吉和街支行)",
                "deviceCode", "b8669a5460da14bb",
                "signLatitude", lat.toString(),
                "dataSource", "APP",
                "assessmentRecordCode", recordCode,
                "signLongitude", lng.toString(),
                "employeeCode", empInfo.getEmployeeCode()
            ))
            .execute();
    }

    private String getReqHead(String url, String token) {
        return "{\"user\":\"\",\"reqTime\":\""
            + System.currentTimeMillis()
            + "\",\"reqUrl\":\""
            + url.replaceAll("/", "\\\\/")
            + "\",\"HRM_ACCESS\":\""
            + token
            + "\",\"RequestRoute\":[{\"system\":\"android\",\"version\":\"28\"},{\"system\":\"MI 6\",\"version\":\"9\"},{\"system\":\"Dpi\",\"version\":\"1080x1920\"},{\"system\":\"xqf_app_Online\",\"version\":\"1.4.3\"},{\"system\":\"App\",\"version\":\"1.4.3\"}]}";
    }


    public static class EmpInfo {
        private String userCode;
        private String userName;
        private String companyCode;
        private String companyName;
        private String employeeCode;

        public String getUserCode() {
            return userCode;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getCompanyCode() {
            return companyCode;
        }

        public void setCompanyCode(String companyCode) {
            this.companyCode = companyCode;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public void setEmployeeCode(String employeeCode) {
            this.employeeCode = employeeCode;
        }
    }

}
