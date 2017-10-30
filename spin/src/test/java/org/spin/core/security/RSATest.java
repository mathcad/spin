package org.spin.core.security;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Arvin on 2017/3/27.
 */
public class RSATest {
    String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdyKzf1RUozDVoVdbw6ZuYC+eFTLjY3wnkyzcVmdnkpEHXy9D0VUs/wqEZKxO3AuLxcV8QwOxtsBjkfzIKIxk29P5JWBhyAuXKRQQooUv8iB3ncN8eK3tpHmawH3a0TgnSPg+3bD37uLqRc2ENEY0qUiBhQkoa0uDMj6/013HJOwIDAQAB";
    String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJ3IrN/VFSjMNWhV1vDpm5gL54VMuNjfCeTLNxWZ2eSkQdfL0PRVSz/CoRkrE7cC4vFxXxDA7G2wGOR/MgojGTb0/klYGHIC5cpFBCihS/yIHedw3x4re2keZrAfdrROCdI+D7dsPfu4upFzYQ0RjSpSIGFCShrS4MyPr/TXcck7AgMBAAECgYEAibDAo8gIYgTqqnUWUEAcRvBEhv/v41moAaARHumWyz9IMjAr1bzFIQwQl60O1EtRjk9YHX+uEv50ipoxKcV9TyfxpBrgZpWkll3JErkNEqt5yepXrPwedY4jYzJiVs2Cnar2hyUTaIphzZqL1uoeeX+0uwexxOLtBKYjNiHNZcECQQDT8nAjM1oTJU/FLFosEsbPfDZmzuCnW6baKBbLX1AHtlHgwcdLta8GX47ApOaZngR57veeOGyN13Sch7+i3EahAkEAvpRAmu2i9YQ4L9ZfLsp6aA/ZjHCd09ttG65jA9NiLjX62pfcyFgAt9eCCU/sBKEWraCl650QPN9OAecCppvuWwJBAIjuV/6V/bri3z+vIO7ajrGcOXWAcOoPL6RAREHOaWEiLJH9/+ltDxAaCptxrj5PNeslNbt2DsQxD/jVRz1L/SECQQCQ4WeT4CBIgXGtfEzz513TCmmaSGrTijaSGqqPV/2Fn+fKkjR34d75482pgqashkIVUNGSIt8bR6+n5pSvUE+NAkBMfjDWrO41NOjwC1HcmDO5cFuUhctAnad6GxIWHLiEn/u17YZkXCTRoVCh5SSuAYpOzbiwbx/c63kO6E6eH5rX";
    String encrypted = "MKAGCn2x2TtP6ByCVTWrBVzIiddcpBYLYRBPbGkfnjX1YdjRPawNXGZpE0N0kv1BqXRuD+dTa+VPlzhmqJoJuML9VpCWMKmpMTRySUgJGleHfRd4HW7vNvsB7ng+rosVD/jrUawfddaMu6hzo7oTz3lYKGlRukZnhj2YIN/Zu80=";

    @Test
    public void testRsaJs() throws InvalidKeyException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException, NoSuchPaddingException, ShortBufferException {
        String dencrypted = RSA.decrypt(privateKey, encrypted);
        System.out.println(dencrypted);

        System.out.println(RSA.encrypt(publicKey, "abcd"));
        assertTrue(true);
    }
}
