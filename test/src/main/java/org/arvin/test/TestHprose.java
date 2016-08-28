package org.arvin.test;

import org.arvin.test.hprose.HproseProxyBeanResolver;
import org.service.demo.DemoService;

/**
 * Created by Arvin on 2016/8/27.
 */
public class TestHprose {
    public static void main(String[] args) {
        HproseProxyBeanResolver factoryBean = new HproseProxyBeanResolver();
        factoryBean.setServiceInterface(DemoService.class);
        factoryBean.setAccessToken("test");
        String serviceUrl = "http://localhost:8080/Demo";
        factoryBean.setServiceUrl(serviceUrl);
        factoryBean.afterPropertiesSet();
        try {
            DemoService service = factoryBean.getServiceBean(DemoService.class);
            System.out.println(service.hello("demo"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
