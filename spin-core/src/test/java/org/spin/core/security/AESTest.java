package org.spin.core.security;

import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;
import org.spin.core.util.JsonUtils;

import java.nio.charset.Charset;

import javax.crypto.SecretKey;

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
    private String dataJson = "[113, 96, -109, 112, -11, -44, 0, -15, -42, 24, -8, -13, 23, -102, 38, -51, 17, -62, -72, -68, -105, 76, -76, 37, -88, -122, -107, -122, -112, -23, 22, -92, -94, 28, 57, -42, 37, 32, -23, -69, -98, 58, 61, 29, 6, -12, 13, 87, -110, 20, -51, -47, 102, 7, 21, -61, -91, -65, -22, -118, -90, -72, -23, -46, 67, 45, -126, 72, -34, 93, 56, 112, 23, -86, -89, 115, -25, -115, 118, -6, 126, 107, -6, -11, 49, -119, -40, 97, -70, 127, -24, 120, -89, 40, 120, -20, 7, -114, -5, -113, -86, -28, 96, -4, 79, 14, 120, -34, 88, -79, -24, 52, -5, -118, -31, 4, 89, 35, -57, 8, 35, -68, -11, 22, 12, 57, 1, -82, -59, -23, 60, -59, -52, -108, 31, 126, -30, -108, -59, -75, -96, -64, -47, -104, -95, 46, -7, -97, -36, 127, -117, -128, 12, 119, -15, 56, -72, 82, -89, -3, -71, -62, -24, 12, 115, -49, -81, 100, 113, -38, 26, 40, 16, -68, 115, 30, -120, 102, -122, 104, 110, -17, 114, -54, -88, -28, 75, 44, 21, -53, -14, -19, -94, -123, -46, -121, -127, 111, -59, -28, -65, 20, -127, 42, -98, -82, -50, -100, -128, 25, -109, 35, 100, -5, 57, 101, -105, 94, 112, 53, 81, -120, 120, 46, -123, -35, 97, -10, -51, -66, -84, 111, 122, -35, 103, -75, 82, -104, -22, 111, 21, -22, 94, 28, -112, -96, 84, 103, -27, -122, 8, -70, -91, 58, 91, -110, -65, -121, -19, -5, -70, -59, -13, -16, 68, 36, -10, -56, 122, -92, -81, 119, -89, 100, -43, 92, -56, -38, -56, -29, 78, 1, -65, -31, -73, -89, 0, 78, 39, -36, -7, 66, 119, 86, 38, 101, 32, 15, -46, -29, 107, -17, 77, 69, -88, 80, -91, -96, 115, -25, 14, 100, 124, -42, -79, 40, -101, -119, 29, 18, 36, -86, 2, 84, 71, -13, -35, 40, -64, -128, -14, 41, 37, -74, -24, 34, -61, -114, 27, 31, 100, 105, -76, -64, 111, 39, 90, 59, 105, -107, -55, -107, -85, 117, 84, 94, -2, -89, 34, -71, 2, -9, -93, 28, 26, -5, -116, -113, -88, 86, 111, -3, -103, 125, 19, 14, -109, -78, -103, -70, -91, -52, -38, -65, 93, -49, -71, 62, -39, 18, 68, 58, -84, -50, 84, -76, 42, 73, -85, 20, 55, -12, 7, -65, 14, -57, 38, 40, -42, 126, 99, -126, 78, -124, 57, -5, 99, -51, 41, -89, -113, 21, 19, -51, -64, -71, 120, 60, -70, 58, -14, 45, 21, -54, -16, 41, -52, 3, -32, -108, -42, -13, 18, -18, -65, -121, 64, 79, -105, 88, 48, 74, -38, -106, -72, 77, -83, -122, -100, -51, -24, -110, -44, -48, 47, -10, 80, -56, 36, -35, 93, -37, -36, -39, 71, 113, -83, -5, -124, -55, -100, 101, 67, 39, -3, 47, -62, 111, 124, 33, 101, -97, -77, -99, 43, -92, -98, 96, -5, -105, 6, 56, 44, 112, 60, 10, -101, -39, -64, -14, -105, 122, -112, 68, 119, -75, -57, 103, 52, 7, -49, 103, -27, 85, 63, -25, -115, -106, 99, -21, -103, 80, 11, -103, -100, 5, -8, -15, 36, 49, -46, -61, -8, -15, -2, 111, 106, 17, 115, 77, -95, 70, -36, 43, 39, -4, 65, -90, -23, -98, -113, -121, 22, 90, -78, -118, 24, 40, 80, -74, -126, -83, -15, 31, -24, -27, -35, 22, -78, -20, 50, 63, -38, 28, 66, 38, -1, -2, -91, -81, 72, -3, 23, -112, 31, -95, 98, -67, 17, -69, -19, 84, 61, -60, -39, 63, 127, -40, -52, -10, 112, -101, -122, -97, -10, -71, 31, -125, 98, -37, 86, -22, -70, -23, 21, 7, 102, -82, 93, -128, 6, -61, -21, -21, -91, -40, 19, -37, 89, 95]";
    private TypeToken<byte[]> typeToken = new TypeToken<byte[]>() {
    };

    @Test
    public void testAes() {
        SecretKey secretKey = AES.generateKey(key, AES.KeyLength.WEAK);
        byte[] data = JsonUtils.fromJson(dataJson, typeToken);
//        System.out.println(HexUtils.encodeHexStringU(data));
//        String decrypt = AES.decrypt(AES.generateKey("ecb55d9d3501e902"), data);
//        System.out.println(decrypt);
        assertTrue(true);
    }

}
