package org.zibra.server;

import org.zibra.common.ZibraContext;
import org.zibra.common.HproseMethods;

import java.lang.reflect.Type;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ZibraHttpMethods extends HproseMethods {

    @Override
    protected int getCount(Type[] paramTypes) {
        int i = paramTypes.length;
        if ((i > 0) && (paramTypes[i - 1] instanceof Class<?>)) {
            Class<?> paramType = (Class<?>) paramTypes[i - 1];
            if (paramType.equals(ZibraContext.class) ||
                    paramType.equals(ServiceContext.class) ||
                    paramType.equals(HttpContext.class) ||
                    paramType.equals(HttpServletRequest.class) ||
                    paramType.equals(HttpServletResponse.class) ||
                    paramType.equals(HttpSession.class) ||
                    paramType.equals(ServletContext.class) ||
                    paramType.equals(ServletConfig.class)) {
                --i;
            }
        }
        return i;
    }
}
