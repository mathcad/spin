package org.spin.cloud.feign;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.JsonUtils;
import org.spin.web.RestfulResponse;
import org.spin.web.throwable.FeignHttpException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static feign.FeignException.errorStatus;
import static feign.Util.RETRY_AFTER;
import static feign.Util.checkNotNull;
import static java.util.Locale.US;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class RestfulErrorDecoder implements ErrorDecoder {
    private static final Logger logger = LoggerFactory.getLogger(RestfulErrorDecoder.class);

    private final RetryAfterDecoder retryAfterDecoder = new RetryAfterDecoder();

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = errorStatus(methodKey, response);
        if (!CollectionUtils.isEmpty(response.headers().get("Encoded"))
            && "1".equals(response.headers().get("Encoded").iterator().next())) {
            String restfulContent = exception.contentUTF8();
            RestfulResponse<?> restfulEnt = JsonUtils.fromJson(restfulContent, RestfulResponse.class);


            logger.warn("Feign 远程服务返回异常: {}-[{}]\n-->{}\n-->{}",
                restfulEnt.getStatus(),
                restfulEnt.getPath(),
                restfulEnt.getError(),
                restfulEnt.getMessage());
            return new FeignHttpException(restfulEnt.getStatus(), restfulEnt.getPath(), restfulEnt.getError(), restfulEnt.getMessage(), exception)
                .withPayload(restfulEnt.getData());
        }
        Date retryAfter = retryAfterDecoder.apply(firstOrNull(response.headers(), RETRY_AFTER));
        if (retryAfter != null) {
            return new RetryableException(
                response.status(),
                exception.getMessage(),
                response.request().httpMethod(),
                exception,
                retryAfter,
                response.request());
        }
        return exception;
    }

    private <T> T firstOrNull(Map<String, Collection<T>> map, String key) {
        if (map.containsKey(key) && !map.get(key).isEmpty()) {
            return map.get(key).iterator().next();
        }
        return null;
    }

    /**
     * Decodes a {@link feign.Util#RETRY_AFTER} header into an absolute date, if possible. <br>
     * See <a href="https://tools.ietf.org/html/rfc2616#section-14.37">Retry-After format</a>
     */
    static class RetryAfterDecoder {

        static final DateFormat RFC822_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", US);
        private final DateFormat rfc822Format;

        RetryAfterDecoder() {
            this(RFC822_FORMAT);
        }

        RetryAfterDecoder(DateFormat rfc822Format) {
            this.rfc822Format = checkNotNull(rfc822Format, "rfc822Format");
        }

        protected long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        /**
         * returns a date that corresponds to the first time a request can be retried.
         *
         * @param retryAfter String in
         *                   <a href="https://tools.ietf.org/html/rfc2616#section-14.37" >Retry-After format</a>
         */
        public Date apply(String retryAfter) {
            if (retryAfter == null) {
                return null;
            }
            if (retryAfter.matches("^[0-9]+\\.?0*$")) {
                retryAfter = retryAfter.replaceAll("\\.0*$", "");
                long deltaMillis = SECONDS.toMillis(Long.parseLong(retryAfter));
                return new Date(currentTimeMillis() + deltaMillis);
            }
            synchronized (rfc822Format) {
                try {
                    return rfc822Format.parse(retryAfter);
                } catch (ParseException ignored) {
                    return null;
                }
            }
        }
    }
}
