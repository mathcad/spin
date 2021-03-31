package org.spin.datasource.exception;

/**
 * exception when  druid dataSource init failed
 *
 * @author TaoYu
 * @since 2.5.6
 */
public class ErrorCreateDataSourceException extends RuntimeException {

    public ErrorCreateDataSourceException(String message) {
        super(message);
    }

    public ErrorCreateDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
