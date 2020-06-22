package org.spin.web.converter;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/6/3</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public interface EncryptParamDecoder {

    /**
     * 解密参数
     *
     * @param encryptParam 密文
     * @return 明文
     */
    String decrypt(String encryptParam);
}
