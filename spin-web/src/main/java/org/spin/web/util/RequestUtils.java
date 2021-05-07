package org.spin.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.spin.web.RestfulResponse;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/5/18</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public final class RequestUtils {
    private static final Logger logger = LoggerFactory.getLogger(RequestUtils.class);

    private RequestUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    public static void error(HttpServletResponse response, ErrorCode errorCode, String... message) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Encoded", "1");
            response.getWriter().write(JsonUtils.toJson(RestfulResponse
                .error(errorCode, ((null == message || message.length == 0 || StringUtils.isEmpty(message[0])) ? errorCode.getDesc() : message[0]))));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
