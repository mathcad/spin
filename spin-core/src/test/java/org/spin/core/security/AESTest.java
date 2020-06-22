package org.spin.core.security;

import org.junit.jupiter.api.Test;
import org.spin.core.util.DigestUtils;
import org.spin.core.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/6/12.</p>
 *
 * @author xuweinan
 */
class AESTest {
    private String key = "ecb55d9d3501e902";

    @Test
    public void testAes() {
        AES aes = AES.newInstance(AES.Mode.CBC, AES.Padding.PKCS7Padding).withIv(StringUtils.getBytesUtf8(key)).withKey(key, AES.KeyLength.STRONG);
        String encrypt = aes.encrypt("message你1");
        System.out.println(encrypt);
        System.out.println(aes.decrypt(encrypt));

        encrypt = aes.encrypt("message你2");
        System.out.println(encrypt);
        System.out.println(aes.decrypt(encrypt));

        aes.withIv(new byte[16]);
        encrypt = aes.encrypt("message你2");
        System.out.println(encrypt);
        System.out.println(aes.decrypt(encrypt));
        assertTrue(true);
    }

    @Test
    void testAes2() {
        String encrypted = "IScFBdcWMuCjSn3W+MfjGY2llPQ1z4MDRh4zMTDlJ5Qwnz1TWfcDQU6wAa8eZA7Ap2U3CfUNcLHxGfmAV3YI15LND5vSjVzD+JcD/B6Pqncwfrf1Lwr5MfgxS0drDo8gR29PD5TQ2jtcx7JcRpylLE+0RYTrhZqyVKw/Oby5JQyQMN4KArio/+++ZDbEQ/tLiI0H/1zPJ4Wy7wZmcSz2SInBUffVBXz+0917fwtVxWZxfA5dd2wCwM8vL/u7H4wfcVXlaf7Q7DpHLjZ/nalzYR76BfrzeqSrxnSjSD5WUMXnaAAq3HtDQ8zS2grDLTUcH0CFvlklUIL7j66aSDhoLsGqNHmJ9NmmmiUt0fsmD6emmhDgxOYpNgzoXV+V/G2BgYD/Ccqjj2uvFAeGMu1MZ9S5Kus9N96YQY3EybtwqjplJ9ah/m7SXXq1xX4J4hoo9SORscy7Sj1UGxayzEgJZ7OuI1sLZV5L/4hPzrtuUpcOKXaLNS/r98/2bsSGMqDdefvj8dvqwsvP7W8fIFeRgFsVK1d6karnw4OkDIQ8Du+oNO0ScVclb/UngMCbnRTjMp/Q2RX1wchsXe7DJjyVX9sBUcnL5M91CFQXtYttpW93peJFGXrie9IUubnjm3jl+XQKM9ffy+5iFWot83mXnGn9vJj3n7ENipnk2nm2NlM1PPUr/m4STlrpu9nze7y51bQbLOXGVk5UmJCquHuTje1woHT+MBwT0xoAm+bfYqcbelPaUxwmYBTxpI+S4OVw9Me+lzmc4Y91I5NfSGzPsHs5WkFX13DJ4LU2xB65h9WveyMNZTjC7uq3T0w9/pr62LRWFbf74r+v7nuYfCqfXmCEEGowWHuXvv3JHxs6jVuPV4PN5Avn2LItiEskuaesOwuUlJBwd4pUm8r5zPCqvMarKW/uN9gFzaNRdNXoCeZ8mxCRfZW/gszGCQ4EVdAxiF1oNZ700csrAU7cuXeCSM/rkJ9Yq9wnEYZCnsQdq3VCInog1Zy7dLSDp7eOnY+9lIRylk/ZuQLNuhuT7VT505GFuEJe9jZraQc7Cn3crnM=";
        String key = "01234567898888866666012345678999";

        SecretKeySpec k = new SecretKeySpec(DigestUtils.md5HexL(key).getBytes(StandardCharsets.UTF_8), "AES");
        String decrypt = AES.newInstance(AES.Mode.ECB, AES.Padding.PKCS7Padding).withKey(k, AES.KeyLength.STRONG).decrypt(encrypted);
        System.out.println(decrypt);
    }

    //    @Test
    void testFile() {
        AES aes = AES.newInstance(AES.Mode.CBC, AES.Padding.PKCS7Padding).withKey(key, AES.KeyLength.STRONG).withIv(StringUtils.getBytesUtf8(key));
        try (InputStream is = new FileInputStream(new File("D:/ubuntu.tar")); OutputStream os = new FileOutputStream(new File("D:/encrypt.txt"))) {
            aes.encrypte(is, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    void testFileB() {
        AES aes = AES.newInstance(AES.Mode.CBC, AES.Padding.PKCS7Padding).withKey(key, AES.KeyLength.STRONG).withIv(StringUtils.getBytesUtf8(key));
        try (InputStream is = new FileInputStream(new File("D:/encrypt.txt")); OutputStream os = new FileOutputStream(new File("D:/decrypt.txt"))) {
            aes.decrypt(is, os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Test
    void testShare() {
        String[] users = {
            "徐韦男",
            "汪洋",
            "邹绍鹏",
            "陈萌",
            "吕超群",
            "陈鹏",
            "湛永胜",
            "张雪芳",
            "肖燕",
            "李朋生",
            "朱昆虬",
            "魏虔坤",
            "周彩霞"
        };

        Random random = ThreadLocalRandom.current();
        System.out.println(users[random.nextInt(13)]);
    }

}
