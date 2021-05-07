package org.spin.data.provider;

import org.hibernate.SessionFactory;
import org.spin.data.annotation.Ds;
import org.spin.web.annotation.RequestDs;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/3/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class WebSessionFactoryProvider extends SessionFactoryProvider {

    @Override
    public SessionFactory getSessionFactory(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();

        RequestDs webDs = AnnotatedElementUtils.getMergedAnnotation(method, RequestDs.class);
        if (null != webDs && !webDs.openSession()) {
            return null;
        }
        Ds ds = null;
        if (null == webDs) {
            ds = AnnotatedElementUtils.getMergedAnnotation(method, Ds.class);
            if (null != ds && !ds.openSession()) {
                return null;
            }
        }


        return lookupSessionFactory(null == webDs ? (null == ds ? null : ds.value()) : webDs.value());
    }
}
