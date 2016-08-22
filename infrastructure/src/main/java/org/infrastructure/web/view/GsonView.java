package org.infrastructure.web.view;

import com.google.gson.*;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.infrastructure.jpa.api.HibernateProxyTypeAdapter;
import org.infrastructure.sys.EnumUtils;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用Gson序列化生成Json返回的View
 *
 * @author zx Nov 17, 2012
 */
public class GsonView extends AbstractView {
    static Logger logger = LoggerFactory.getLogger(GsonView.class);
    static final String JSON_DATA_KEY = "JSON$DATA";

    private Gson _gson = null;

    /**
     * 是否需要JPA代理类型序列化
     */
    public static boolean JPA_TYPE_CONVERT = true;

    public Gson getGson() {

        if (_gson == null) {
            GsonBuilder gb = new GsonBuilder();
            regGson(gb);
            _gson = gb.create();
        }
        return _gson;
    }

    /**
     * 使用html的方式返回Json
     */
    boolean htmlContentType = false;

    /**
     * 返回GsonView
     */
    public GsonView(boolean htmlContentType) {
        this.htmlContentType = htmlContentType;
    }

    /**
     * 以Json方式返回 Map中的内容
     */
    public GsonView() {
        this(false);
    }

    /**
     * 以何种api方式调用 若为get方式调用初始化， UserEnum初始化为int类型
     */
    public static void setApi(Object v) {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            attr.getRequest().setAttribute("api.action", v);
        } catch (Exception e) {
            logger.info("No ServletRequestAttributes");
        }
    }

    /**
     * 以何种api方式调用 若为get方式调用初始化， UserEnum初始化为int类型
     */
    public static Object getApi() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attr.getRequest().getAttribute("api.action");
        } catch (Exception e) {
            logger.info("No ServletRequestAttributes");
            return null;
        }
    }

    /**
     * 注入类型的转化控制，调用了以下两个部分 regSimpleTypes regJpaProxyType Date / Enum 等
     */
    public static void regGson(GsonBuilder gb) {
        regSimpleTypes(gb);

        if (JPA_TYPE_CONVERT)
            regJpaProxyType(gb);
    }

    /**
     * 注入Jpa简单类型处理
     * <p>
     * 枚举、日期、字段等等
     */
    public static void regSimpleTypes(GsonBuilder gb) {
        /* 通用的java.sql.Time 转换 */
        class TimeSerializer implements JsonSerializer<Time>, JsonDeserializer<Time> {
            SimpleDateFormat shortFmt = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat longFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public synchronized Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                String str = json.getAsString();
                if (StringUtils.isEmpty(str))
                    return null;

                SimpleDateFormat dateFmt;
                if (str.length() == 8)
                    dateFmt = shortFmt;
                else if (str.length() == 19)
                    dateFmt = longFmt;
                else
                    throw new BizException("Time长度不支持Json解析：" + str);

                try {
                    return new Time(dateFmt.parse(str).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new BizException("TimeJson解析失败：" + e.getMessage());
                }
            }

            @Override
            public synchronized JsonElement serialize(Time src, Type typeOfSrc, JsonSerializationContext context) {
                if (src != null) {
                    String s = shortFmt.format(src);
                    return new JsonPrimitive(s);
                } else
                    return null;
            }
        }
        gb.registerTypeAdapter(Time.class, new TimeSerializer());

		/* 通用的java.sql.Date转换 */
        class SqlDateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {
            SimpleDateFormat shortFmt = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat longFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public synchronized Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                String str = json.getAsString();
                if (StringUtils.isEmpty(str))
                    return null;

                SimpleDateFormat dateFmt;
                if (str.length() == 10)
                    dateFmt = shortFmt;
                else if (str.length() == 19)
                    dateFmt = longFmt;
                else
                    throw new BizException("Date长度不支持Json解析：" + str);

                try {
                    return new Date(dateFmt.parse(str).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new BizException("日期Json解析失败：" + e.getMessage());
                }
            }

            @Override
            public synchronized JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                if (src != null) {
                    String s = shortFmt.format(src);
                    return new JsonPrimitive(s);
                } else
                    return null;
            }
        }
        gb.registerTypeAdapter(Date.class, new SqlDateSerializer());

		/* 通用的java.util.Date转换 */
        class DateUtilSerializer implements JsonSerializer<java.util.Date>, JsonDeserializer<java.util.Date> {
            SimpleDateFormat shortFmt = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat longFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public synchronized java.util.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                String str = json.getAsString();
                if (StringUtils.isEmpty(str))
                    return null;

                SimpleDateFormat dateFmt;
                if (str.length() == 10)
                    dateFmt = shortFmt;
                else if (str.length() == 19)
                    dateFmt = longFmt;
                else
                    throw new BizException("Date长度不支持Json解析：" + str);

                try {
                    return new Date(dateFmt.parse(str).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new BizException("日期Json解析失败：" + e.getMessage());
                }
            }

            @Override
            public synchronized JsonElement serialize(java.util.Date src, Type typeOfSrc,
                                                      JsonSerializationContext context) {
                if (src != null) {
                    String s = longFmt.format(src);
                    // logger.info("serialize Date:" + s);
                    return new JsonPrimitive(s);
                } else
                    return null;
            }
        }
        gb.registerTypeAdapter(java.util.Date.class, new DateUtilSerializer());

		/* 通用的Timestamp转换 */
        class TimestampSerializer implements JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
            SimpleDateFormat shortFmt = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat longFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public synchronized Timestamp deserialize(JsonElement json, Type typeOfT,
                                                      JsonDeserializationContext context) throws JsonParseException {
                String str = json.getAsString();
                if (StringUtils.isEmpty(str))
                    return null;

                SimpleDateFormat dateFmt;
                if (str.length() == 10)
                    dateFmt = shortFmt;
                else if (str.length() == 19)
                    dateFmt = longFmt;
                else
                    throw new BizException("Timestamp长度不支持Json解析：" + str);

                try {
                    return new Timestamp(dateFmt.parse(str).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new BizException("TimestampJson解析失败：" + e.getMessage());
                }
            }

            @Override
            public synchronized JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {

                if (src != null) {
                    String s = longFmt.format(src);
                    // logger.info("serialize Timestamp:" + s);
                    return new JsonPrimitive(s);
                } else
                    return null;
            }

        }
        gb.registerTypeAdapter(Timestamp.class, new TimestampSerializer());

		/* 通用的枚举转换 */
        class EnumSerializer implements JsonSerializer<Enum>, JsonDeserializer<Enum> {

            @Override
            public JsonElement serialize(Enum src, Type t, JsonSerializationContext context) {
                Class enumCls = ((Class) t);
                Object api = getApi();
                if ("get".equals(api)) {
                    return new JsonPrimitive(EnumUtils.getEnumValue(enumCls, src));
                } else {
                    return new JsonPrimitive(src.toString());
                }
            }

            @Override
            public Enum deserialize(JsonElement j, Type t, JsonDeserializationContext c) throws JsonParseException {
                Class enumCls = ((Class) t);
                if (((JsonPrimitive) j).isNumber()) {
                    int value = j.getAsInt();
                    return EnumUtils.getEnum(enumCls, value);
                } else {
                    String text = j.getAsString();
                    return EnumUtils.getEnum(enumCls, text);
                }
            }
        }

        gb.registerTypeHierarchyAdapter(Enum.class, new EnumSerializer());
    }

    /**
     * 注入Jpa代理的序列化处理
     */
    public static void regJpaProxyType(GsonBuilder gb) {
        gb.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY);

		/* 通用的枚举转换 */
        class AbstractPersistentCollectionSerializer implements JsonSerializer<AbstractPersistentCollection>,
                JsonDeserializer<AbstractPersistentCollection> {

            @Override
            public JsonElement serialize(AbstractPersistentCollection src, Type t, JsonSerializationContext context) {
                logger.info("lazy AbstractPersistentCollection");
                return new JsonArray();
            }

            @Override
            public AbstractPersistentCollection deserialize(JsonElement j, Type t, JsonDeserializationContext c)
                    throws JsonParseException {
                return null;
            }
        }

        gb.registerTypeHierarchyAdapter(AbstractPersistentCollection.class,
                new AbstractPersistentCollectionSerializer());
    }

    /**
     * render Json 输出
     */
    @Override
    protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        Set<String> removeKeys = map.keySet().stream().filter(k -> k.startsWith("org.springframework.validation.BindingResult")).collect(Collectors.toSet());

        // 移除不必要的绑定值
        removeKeys.forEach(map::remove);
        map.remove("org.springframework.validation.BeanPropertyBindingResult");

        Object jsonData = map.containsKey(JSON_DATA_KEY) ? map.get(JSON_DATA_KEY) : map;

        if (this.htmlContentType)
            this.renderJsonAsHtml(jsonData, resp);
        else
            this.renderJson(jsonData, resp);
    }

    /**
     * 输出Json
     */
    protected void renderJson(Object json, HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (!(json instanceof String)) {
            Gson _gson = getGson();
            json = _gson.toJson(json);
        }

        PrintWriter out;
        try {
            out = resp.getWriter();
            out.print((String) json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 输出utf-8编码的html内容
     */
    protected void renderHtml(String responseContent, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(responseContent);
    }

    /**
     * 用 text/html 的方式来输出 json
     */
    protected String renderJsonAsHtml(Object json, HttpServletResponse response) {

        try {
            if (!(json instanceof String)) {
                json = getGson().toJson(json);
            }
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.print((String) json);
            return (String) json;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
