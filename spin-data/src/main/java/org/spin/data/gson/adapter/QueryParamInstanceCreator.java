package org.spin.data.gson.adapter;

import org.spin.core.gson.InstanceCreator;
import org.spin.data.query.QueryParam;

import java.lang.reflect.Type;

/**
 * <p>Created by xuweinan on 2017/9/22.</p>
 *
 * @author xuweinan
 */
public class QueryParamInstanceCreator implements InstanceCreator<QueryParam> {

    @Override
    public QueryParam createInstance(Type type) {
        return QueryParam.from("");
    }
}
