package org.spin.web.handler.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.ErrorCode;
import org.spin.web.RestfulResponse;
import org.spin.web.handler.WebExceptionHalder;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/10/17</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SQLIntegrityConstraintViolationExceptionHandler implements WebExceptionHalder {
    private static final Logger logger = LoggerFactory.getLogger(SQLIntegrityConstraintViolationExceptionHandler.class);

    @Override
    public RestfulResponse<Void> handler(Throwable e, HttpServletRequest request) {
        String msg = "数据重复";
        logger.warn(msg, e);
        return RestfulResponse.error(ErrorCode.INTERNAL_ERROR, msg, e.getMessage());
    }

    @Override
    public boolean support(Throwable e) {
        return e instanceof SQLIntegrityConstraintViolationException;
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE + 3;
    }
}
