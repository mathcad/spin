package org.spin.core.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.jupiter.api.Test;
import org.spin.core.util.MapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void httpGetRequest() {
        String url = "http://www.baidu.com";
        String res = HttpUtils.get(url, MapUtils.ofMap("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.2.1; zh-cn; GT-S5660 Build/GINGERBREAD) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1 MicroMessenger/4.5.255"));
        System.out.println(res);
        assertTrue(true);
    }

    @Test
    public void httpGetRequestAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String url = "http://www.apache.org/";
        Future<HttpResponse> future = HttpUtils.getAsync(url,
            r -> {
                System.out.println(r);
                latch.countDown();
            }
        );
        System.out.println(future.isDone());
        latch.await();
        System.out.println(future.isDone());
        assertTrue(true);
    }

    @Test
    public void oneReuest() {
        final RequestConfig requestConfitg = RequestConfig.custom()
            .setSocketTimeout(3000)
            .setConnectTimeout(3000).build();

        final CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
            .setDefaultRequestConfig(requestConfitg)
            .build();
        httpClient.start();
        final HttpGet request = new HttpGet("http://www.apache.org/");

        CountDownLatch latch = new CountDownLatch(1);

        final Future future = httpClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse obj) {
                latch.countDown();
                System.out.println(request.getRequestLine() + "->" + obj.getStatusLine());
            }

            @Override
            public void failed(Exception excptn) {
                latch.countDown();
                System.out.println(request.getRequestLine() + "->" + excptn);
            }

            @Override
            public void cancelled() {
                latch.countDown();
                System.out.println(request.getRequestLine() + "cancelled");
            }
        });
        try {
            HttpResponse response = (HttpResponse) future.get();
            System.out.println("Response:" + response.getStatusLine());
            latch.await();
            System.out.println("Shutting down");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("执行完毕");
    }

    @Test
    public void moreRequest() {

        final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();

        httpClient.start();

        final HttpGet[] requests = new HttpGet[]{
            new HttpGet("http://www.apache.org/"),
            new HttpGet("http://www.baidu.com/"),
            new HttpGet("http://www.oschina.net/")
        };

        final CountDownLatch latch = new CountDownLatch(requests.length);
        for (final HttpGet request : requests) {

            httpClient.execute(request, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse obj) {
                    latch.countDown();
                    System.out.println(request.getRequestLine() + "->" + obj.getStatusLine());
                }

                @Override
                public void failed(Exception excptn) {
                    latch.countDown();
                    System.out.println(request.getRequestLine() + "->" + excptn);
                }

                @Override
                public void cancelled() {
                    latch.countDown();
                    System.out.println(request.getRequestLine() + "cancelled");
                }
            });
        }

        try {
            latch.await();
            System.out.println("Shutting Down");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Finish!");
    }

    @Test
    public void moreRequestByUtil() {
        final String[] requests = new String[]{
            "http://www.apache.org/",
            "http://www.baidu.com/",
            "http://www.oschina.net/"
        };

        final CountDownLatch latch = new CountDownLatch(requests.length);
        for (final String request : requests) {

            HttpUtils.getAsync(request, r -> {
                latch.countDown();
                System.out.println("test");
            }, e -> {
                latch.countDown();
                e.printStackTrace();
            });

        }

        try {
            latch.await();
            System.out.println("Shutting Down");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("Finish!");
    }


    @Test
    public void test1() {
        String url = "http://192.168.12.54:9200/mall_goods/_analyze";
        Map<String, Object> param = MapUtils.ofMap("field", "ware_name", "text", "华为");
        Map<String, String> head = MapUtils.ofMap("Content-Type", "application/json");
        String s = HttpUtils.postJson(url, head, param);
        System.out.println(s);

    }


    private ExecutorService executorService = Executors.newFixedThreadPool(10);

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
            executorService.submit(() -> {
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
            executorService.submit(() -> {
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
}
