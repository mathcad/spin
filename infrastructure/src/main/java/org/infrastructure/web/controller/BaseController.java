package org.infrastructure.web.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.infrastructure.jpa.api.CmdContext;
import org.infrastructure.jpa.api.CmdParser;
import org.infrastructure.jpa.api.QParamHandler;
import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.core.ARepository;
import org.infrastructure.jpa.dto.Page;
import org.infrastructure.shiro.SessionManager;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.StringUtils;
import org.infrastructure.web.view.ExcelExtGrid;
import org.infrastructure.web.view.GsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Spring MVC基础Action 封装了日期等 Restfull 格式化 Gson 实体转换
 *
 * @author xuweinan
 */
public class BaseController {

    private static Logger logger = LoggerFactory.getLogger(BaseController.class);

    public static Type GTYPE_LIST_MAP = new TypeToken<List<Map>>() {
    }.getType();

    private Gson _gson = null;

    public Gson getGson() {

        if (_gson == null) {
            GsonBuilder gb = new GsonBuilder();
            GsonView.regGson(gb);
            _gson = gb.create();
        }
        return _gson;
    }

    @Autowired
    protected SessionManager sessMgr;

    protected CmdContext cmdContext;

    protected CmdParser cmdParser;

    /**
     * 输出utf-8编码的html内容
     *
     * @param responseContent
     * @param response
     * @throws IOException
     */
    protected void renderHtml(String responseContent, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(responseContent);
    }

    /**
     * 用 text/html 的方式来输出 json
     *
     * @param json     需要序列化对象
     * @param response
     * @return
     */
    protected String renderJsonAsHtml(Object json, HttpServletResponse response) {

        try {
            if (json instanceof String == false) {
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

    /**
     * 从 post的Json参数中获取到待更新的实体类
     *
     * @param enCls
     * @return
     */
    public Object getEntityFromJson(Class enCls) {
        String json = this.sessMgr.getRequest().getParameter("json");
        try {
            Object en = this.getGson().fromJson(json, enCls);
            return en;
        } catch (Exception ex) {
            logger.error("实体Json转换错误:\n" + json);
            throw new BizException("提交的Json数据转换错误", ex);
        }
    }

    /**
     * 从req的q获取p
     *
     * @return
     */
    protected QueryParam getQ() {
        String q = this.sessMgr.getRequest().getParameter("q");
        QueryParam p = this.getGson().fromJson(q, MetaController.TYPE_QPARAM);

        //全局屏蔽头尾空格占位 add by zx 2016-6-17
        for (Object k : p.q.keySet()) {
            String qv = p.q.get(k) == null ? null : p.q.get(k).toString();
            p.q.put(k, StringUtils.trimWhitespace(qv));
        }
        return p;
    }

    /**
     * 利用QParam映射属性查询条件，并且可以自定义查询条件解析
     *
     * @param repo     实体仓储
     * @param handlers 自定义查询类
     * @return
     */
    public Page findByQ(ARepository repo, QParamHandler... handlers) {
        String q = this.sessMgr.getRequest().getParameter("q");
        QueryParam p = this.getGson().fromJson(q, MetaController.TYPE_QPARAM);
        try {
            return MetaController.listByQ(cmdParser, p, repo, handlers);
        } catch (Exception e) {
            throw new BizException("rest查询出错", e);
        }
    }

    /**
     * 从Json转换到 grid实体
     *
     * @param columns json字符串
     * @return
     * @throws Exception
     */
    protected ExcelExtGrid toExcelExtGrid(String columns) throws Exception {
        try {
            ExcelExtGrid grid = this.getGson().fromJson(columns, new TypeToken<ExcelExtGrid>() {
            }.getType());
            grid.columns.get(0).width += 10;
            return grid;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("转化提交的GridColumn出错");
        }
    }

    /**
     * 得到当前会话
     *
     * @return
     */
    public HttpSession getSession() {
        return this.sessMgr.getRequest().getSession();
    }

    /**
     * 获取请求参数
     *
     * @param name
     * @return
     */
    public String getParam(String name) {
        return sessMgr.getRequest().getParameter(name);
    }

}
