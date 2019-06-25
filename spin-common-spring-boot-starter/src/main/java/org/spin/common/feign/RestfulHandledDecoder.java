package org.spin.common.feign;

import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.throwable.BizException;
import org.spin.common.web.RestfulResponse;
import org.spin.core.ErrorCode;
import org.spin.core.gson.internal.$Gson$Types;
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

import static org.spin.common.feign.FeignUtils.getHttpHeaders;

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

    private ObjectFactory<HttpMessageConverters> messageConverters;

    public RestfulHandledDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    @Override
    public Object decode(final Response response, Type type) throws IOException {
        if (type instanceof Class || type instanceof ParameterizedType || type instanceof WildcardType) {

            Type actType = type;
            boolean wrapped = false;
            Collection<String> eTag = response.headers().get("Encoded");
            if (null != eTag && eTag.stream().anyMatch(it -> it.equals("1"))
                && (!(type instanceof ParameterizedType) || (((ParameterizedType) type).getRawType() != RestfulResponse.class))) {
                actType = $Gson$Types.newParameterizedTypeWithOwner(null, RestfulResponse.class, type);
                wrapped = true;
            }
            @SuppressWarnings({"unchecked", "rawtypes"})
            HttpMessageConverterExtractor<?> extractor = new HttpMessageConverterExtractor(actType, this.messageConverters.getObject().getConverters());

            Object data = extractor.extractData(new FeignResponseAdapter(response));
            if (data instanceof RestfulResponse) {
                if (((RestfulResponse) data).getStatus() != ErrorCode.OK.getCode()) {
                    logger.error("Path: {}", ((RestfulResponse) data).getPath());
                    logger.error("Error Message: {}", ((RestfulResponse) data).getError());
                    throw new BizException(new ErrorCode(((RestfulResponse) data).getStatus(), ""), ((RestfulResponse) data).getMessage());
                }
                if (wrapped) {
                    data = ((RestfulResponse) data).getData();
                }
            }
            return data;
        }
        throw new DecodeException(ErrorCode.SERIALIZE_EXCEPTION.getCode(), "type is not an instance of Class or ParameterizedType: " + type);
    }

    private final class FeignResponseAdapter implements ClientHttpResponse {

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
