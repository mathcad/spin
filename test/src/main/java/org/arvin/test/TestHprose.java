package org.arvin.test;

import org.service.demo.DemoService;
import org.zibra.spring.ZibraProxyBeanResolver;

/**
 * Created by Arvin on 2016/8/27.
 */
public class TestHprose {
    public static void main(String[] args) {
        ZibraProxyBeanResolver factoryBean = new ZibraProxyBeanResolver();
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
