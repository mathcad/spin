package org.spin.cloud.config.properties;

import springfox.documentation.spi.DocumentationType;

/**
 * TITLE
 * <p>
 * DESCRIPTION
 * <p>
 * Created by xuweinan on 2022/3/25
 *
 * @author xuweinan
 * @version 1.0
 */
public enum DocType {
    SWAGGER_12,
    SWAGGER_2,
    OAS_30,
    ;

    public DocumentationType documentType() {
        switch (this) {
            case SWAGGER_12:
                return DocumentationType.SWAGGER_12;
            case SWAGGER_2:
                return DocumentationType.SWAGGER_2;
            case OAS_30:
                return DocumentationType.OAS_30;
        }
        return null;
    }
}
