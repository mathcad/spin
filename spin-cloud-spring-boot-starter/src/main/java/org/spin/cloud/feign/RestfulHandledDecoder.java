package org.spin.cloud.feign;

import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.gson.internal.$Gson$Types;
import org.spin.web.RestfulResponse;
import org.spin.web.throwable.FeignHttpException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpMessageConverterExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import static org.spin.cloud.feign.FeignUtils.getHttpHeaders;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/19</p>
 *
 * @author Spencer Gibb
 * @author xuweinan
 */
public class RestfulHandledDecoder implements Decoder {
    private static final Logger logger = LoggerFactory.getLogger(RestfulHandledDecoder.class);

    private final ObjectFactory<HttpMessageConverters> messageConverters;

    public RestfulHandledDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object decode(final Response response, Type type) throws IOException {
        if (type instanceof Class || type instanceof ParameterizedType || type instanceof WildcardType) {

            Type actType = type;
            boolean wrapped = false;
            Collection<String> encoded = response.headers().get("Encoded");
            if (null != encoded && encoded.contains("1")
                && (!(type instanceof ParameterizedType) || (((ParameterizedType) type).getRawType() != RestfulResponse.class))) {
                actType = $Gson$Types.newParameterizedTypeWithOwner(null, RestfulResponse.class, type);
                wrapped = true;
            }
            HttpMessageConverterExtractor<?> extractor = new HttpMessageConverterExtractor(actType, this.messageConverters.getObject().getConverters());

            Object data = extractor.extractData(new FeignResponseAdapter(response));
            if (data instanceof RestfulResponse) {
                if (((RestfulResponse) data).getStatus() != ErrorCode.OK.getCode()) {
                    FeignHttpException exception = new FeignHttpException(((RestfulResponse) data).getStatus(), ((RestfulResponse) data).getPath(), ((RestfulResponse) data).getError(), ((RestfulResponse) data).getMessage(), null);
                    logger.warn("Feign 远程服务返回异常: {}-[{}]\n-->{}\n-->{}",
                        exception.getStatus(),
                        exception.getPath(),
                        exception.getError(),
                        exception.getMessage());
                    throw exception;
                }
                if (wrapped) {
                    data = ((RestfulResponse) data).getData();
                }
            }
            return data;
        }
        throw new DecodeException(ErrorCode.SERIALIZE_EXCEPTION.getCode(), "type is not an instance of Class or ParameterizedType: " + type, response.request());
    }

    private static final class FeignResponseAdapter implements ClientHttpResponse {

        private final Response response;

        private FeignResponseAdapter(Response response) {
            this.response = response;
        }

        @Override
        public HttpStatus getStatusCode() {
            return HttpStatus.valueOf(this.response.status());
        }

        @Override
        public int getRawStatusCode() {
            return this.response.status();
        }

        @Override
        public String getStatusText() {
            return this.response.reason();
        }

        @Override
        public void close() {
            try {
                this.response.body().close();
            } catch (IOException ex) {
                // Ignore exception on close...
            }
        }

        @Override
        public InputStream getBody() throws IOException {
            return this.response.body().asInputStream();
        }

        @Override
        public HttpHeaders getHeaders() {
            return getHttpHeaders(this.response.headers());
        }

    }
}
