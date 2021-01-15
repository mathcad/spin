package org.spin.web.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.IOUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.converter.EncryptParamDecoder;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DecryptHttpInputMessage implements HttpInputMessage {
    private static final Logger logger = LoggerFactory.getLogger(DecryptHttpInputMessage.class);

    private final HttpInputMessage inputMessage;
    private final EncryptParamDecoder decoder;
    private final MethodParameter parameter;

    public DecryptHttpInputMessage(HttpInputMessage inputMessage, EncryptParamDecoder decoder, MethodParameter parameter) {
        this.inputMessage = inputMessage;
        this.decoder = decoder;
        this.parameter = parameter;
    }

    @Override
    public @NonNull
    InputStream getBody() throws IOException {
        String content = IOUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);

        try {
            String decryptBody = decoder.decrypt(content);
            return new ByteArrayInputStream(StringUtils.getBytesUtf8(decryptBody));
        } catch (Exception e) {
            logger.warn("参数解密失败: ", e);
            throw new SimplifiedException("参数[" + parameter.getParameterName() + "]解密失败");
        }
    }

    @Override
    public @NonNull
    HttpHeaders getHeaders() {
        return inputMessage.getHeaders();
    }
}
