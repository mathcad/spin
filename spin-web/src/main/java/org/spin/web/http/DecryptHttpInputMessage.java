package org.spin.web.http;

import org.spin.core.util.IOUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.converter.EncryptParamDecoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DecryptHttpInputMessage implements HttpInputMessage {
    private final HttpInputMessage inputMessage;
    private final EncryptParamDecoder decoder;

    public DecryptHttpInputMessage(HttpInputMessage inputMessage, EncryptParamDecoder decoder) {
        this.inputMessage = inputMessage;
        this.decoder = decoder;
    }

    @Override
    public @NonNull
    InputStream getBody() throws IOException {
        String content = IOUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);

        String decryptBody = decoder.decrypt(content);

        return new ByteArrayInputStream(StringUtils.getBytesUtf8(decryptBody));
    }

    @Override
    public @NonNull
    HttpHeaders getHeaders() {
        return inputMessage.getHeaders();
    }
}
