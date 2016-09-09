package org.infrastructure.web.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.infrastructure.jpa.api.RepositoryContext;
import org.infrastructure.jpa.api.QueryParamParser;
import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.api.QueryParamHandler;
import org.infrastructure.jpa.core.ARepository;
import org.infrastructure.jpa.core.Page;
import org.infrastructure.shiro.SessionManager;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.StringUtils;
import org.infrastructure.web.view.ExcelExtGrid;
import org.infrastructure.web.view.GsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Spring MVC基础Action 封装了日期等 Restfull 格式化 Gson 实体转换
 *
 * @author xuweinan
 */
public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

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

    @Autowired
    protected RepositoryContext repositoryContext;

    protected QueryParamParser queryParamParser;

    /**
     * 输出utf-8编码的html内容
     */
    protected void renderHtml(String responseContent, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(responseContent);
    }

    /**
     * 用 text/html 的方式来输出 json
     *
     * @param json 需要序列化对象
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

    /**
     * 从 post的Json参数中获取到待更新的实体类
     */
    public Object getEntityFromJson(Class enCls) {
        String json = this.sessMgr.getRequest().getParameter("json");
        try {
            return this.getGson().fromJson(json, enCls);
        } catch (Exception ex) {
            logger.error("实体Json转换错误:\n" + json);
            throw new SimplifiedException("提交的Json数据转换错误", ex);
        }
    }

    /**
     * 从req的q获取p
     */
    protected QueryParam getQ() {
        String q = this.sessMgr.getRequest().getParameter("q");
        QueryParam p = this.getGson().fromJson(q, QueryParam.class);

        //全局屏蔽头尾空格占位
        for (String k : p.getConditions().keySet()) {
            String qv = p.getConditions().get(k) == null ? null : p.getConditions().get(k);
            p.getConditions().put(k, StringUtils.trimWhitespace(qv));
        }
        return p;
    }

    /**
     * 利用QParam映射属性查询条件，并且可以自定义查询条件解析
     *
     * @param repo     实体仓储
     * @param handlers 自定义查询类
     */
    public Page findByQ(ARepository repo, QueryParamHandler... handlers) {
        String q = this.sessMgr.getRequest().getParameter("q");
        QueryParam p = this.getGson().fromJson(q, QueryParam.class);
        try {
            return listByQ(queryParamParser, p, repo, handlers);
        } catch (Exception e) {
            throw new SimplifiedException("rest查询出错", e);
        }
    }

    /**
     * 从Json转换到 grid实体
     *
     * @param columns json字符串
     */
    protected ExcelExtGrid toExcelExtGrid(String columns) throws Exception {
        try {
            ExcelExtGrid grid = this.getGson().fromJson(columns, new TypeToken<ExcelExtGrid>() {
            }.getType());
            grid.columns.get(0).width += 10;
            return grid;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SimplifiedException("转化提交的GridColumn出错");
        }
    }

    /**
     * 得到当前会话
     */
    public HttpSession getSession() {
        return this.sessMgr.getRequest().getSession();
    }

    /**
     * 获取请求参数
     */
    public String getParam(String name) {
        return sessMgr.getRequest().getParameter(name);
    }

    /**
     * 执行实体带fields投影的查询
     *
     * @param queryParamParser sql解析器实例
     * @param p         通用查询参数
     * @param repo      Dao
     * @param handlers  自定义查询类
     * @return 查询结果
     */
    public static Page listByQ(QueryParamParser queryParamParser, QueryParam p, ARepository repo, QueryParamHandler... handlers) throws Exception {
        QueryParamParser.DetachedCriteriaResult dr = queryParamParser.parseDetachedCriteria(p, handlers);
        DetachedCriteria dc = dr.dc;
        int page = p.getStart() / p.getLimit();
        int pagesize = p.getLimit();
        PageRequest pr = new PageRequest(page, pagesize);
        Order[] orders = queryParamParser.parseOrders(p);

        Page result;
        if (p.getFields() == null || p.getFields().size() == 0) {
            // 实体列表查询，不推荐（性能低下）
            result = repo.find(dc, pr, orders);
        } else {
            result = repo.findByFields(p.getFields(), dr, pr, orders);
        }
        return result;
    }
}
