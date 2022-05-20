package org.spin.datasource.exception;

/**
 * exception when dataSource cannot select
 *
 * @author TaoYu
 * @since 2.5.6
 */
public class CannotFindDataSourceException extends RuntimeException {

    public CannotFindDataSourceException(String message) {
        super(message);
    }

    public CannotFindDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
