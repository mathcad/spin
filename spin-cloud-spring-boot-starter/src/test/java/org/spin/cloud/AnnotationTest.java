package org.spin.cloud;

import org.junit.jupiter.api.Test;
import org.spin.core.util.DateUtils;
import org.spin.core.util.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class AnnotationTest {

    @Test
    void testSwitch() {
        String str = "a";
        switch (str) {
            default:
                System.out.println("default");
            case "b":
                System.out.println("1");
            case "a":
                System.out.println("2");
            case "c":
                System.out.println("3");
        }
    }

    @Test
    void testSwitch2() {
        int str = 2;
        switch (str) {
            default:
                System.out.println("default");
            case 1:
                System.out.println("1");
                break;
            case 2:
                System.out.println("2");
                break;
            case 3:
                System.out.println("3");
                break;
        }
    }

    @Test
    void testProperties() {
        Properties properties = new Properties();
        Properties resolver = new Properties();
        String s = SystemUtils.USER_HOME + File.separator + "feign-resolve.properties";
        try (InputStream is = new FileInputStream(new File(s))) {
            properties.load(is);
            properties.forEach((k, v) -> resolver.put(k.toString().toUpperCase(), v));
        } catch (Exception e) {
            // do nothing
        }
    }

    @Test
    void testCleanField() {
//        Page<MailVo> page = new Page<>();
//        page.setRows(new LinkedList<>());
//
//        MailVo mailVo = new MailVo();
//        mailVo.setReceivers(new LinkedList<>());
//        MailReceiverVo receiverVo = new MailReceiverVo();
//        receiverVo.setReceiver("r11");
//        receiverVo.setEnterpriseId(0L);
//        mailVo.getReceivers().add(receiverVo);
//        receiverVo = new MailReceiverVo();
//        receiverVo.setReceiver("r12");
//        receiverVo.setEnterpriseId(0L);
//        mailVo.getReceivers().add(receiverVo);
//        page.getRows().add(mailVo);
//
//
//        mailVo = new MailVo();
//        mailVo.setReceivers(new LinkedList<>());
//        receiverVo = new MailReceiverVo();
//        receiverVo.setReceiver("r21");
//        receiverVo.setEnterpriseId(0L);
//        mailVo.getReceivers().add(receiverVo);
//        receiverVo = new MailReceiverVo();
//        receiverVo.setReceiver("r22");
//        receiverVo.setEnterpriseId(0L);
//        mailVo.getReceivers().add(receiverVo);
//        page.getRows().add(mailVo);
//
//        System.out.println(JsonUtils.toJson(page));
//
//        FieldPermissionReturnValueModifier.setFieldValue(page, "rows[*].receivers[*].receiver");
//        System.out.println(JsonUtils.toJson(page));

    }

    @Test
    void testDate() {
        long l = System.currentTimeMillis();
        System.out.println(DateUtils.formatDateForSecond(new Date(l)));
        System.out.println(DateUtils.formatDateForSecond(LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault())));

    }
}
