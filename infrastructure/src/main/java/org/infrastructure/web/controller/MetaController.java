package org.infrastructure.web.controller;

import java.lang.reflect.Type;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.infrastructure.jpa.api.CmdContext;
import org.infrastructure.jpa.api.CmdParser;
import org.infrastructure.jpa.api.CmdParser.DetachedCriteriaResult;
import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.api.QParamHandler;
import org.infrastructure.jpa.core.ARepository;
import org.infrastructure.jpa.dto.Page;
import org.infrastructure.sys.ElUtils;
import org.infrastructure.throwable.BizException;
import org.infrastructure.web.view.ExcelExportView;
import org.infrastructure.web.view.ExcelExtGrid;
import org.infrastructure.web.view.GsonView;
import org.infrastructure.web.view.ModelGsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.reflect.TypeToken;

/**
 * 提供直接操控元数据的接口
 * <p>
 * 动态映射crud操作的rest full api
 *
 * @author xuweinan
 */
public abstract class MetaController extends BaseController {
    static Logger logger = LoggerFactory.getLogger(MetaController.class);

    public static final Type TYPE_QPARAM = new TypeToken<QueryParam<String>>() {
    }.getType();

    @Autowired
    CmdContext cmdContext;

    @Autowired
    CmdParser cmdParser;

    /**
     * @param q
     * @return
     */
    @RequestMapping("get")
    public ModelGsonView get(@RequestParam String cls, @RequestParam Long id,
                             @RequestParam(defaultValue = "1", required = false) Integer depth,
                             @RequestParam(defaultValue = "true", required = false) boolean get) {
        ModelGsonView mv = new ModelGsonView();
        try {
            // 标记正在执行 get对象查询，枚举显示为值
            if (get)
                GsonView.setApi("get");
            ARepository repo = cmdContext.getRepo(cls);
            depth = depth > 2 ? 2 : depth;
            mv.ok().add("data", repo.get(id, depth));
        } catch (Throwable e) {
            logger.error("api查询异常", e);
            mv.error(e.getMessage());
        }
        return mv;
    }

    /**
     * @param q
     * @return
     */
    @RequestMapping("list")
    public ModelGsonView list(String q) {
        ModelGsonView mv = new ModelGsonView();
        try {
            if (StringUtils.isEmpty(q))
                throw new BizException("空查询无法执行");

            QueryParam p = this.getGson().fromJson(q, TYPE_QPARAM);
            ARepository repo = cmdContext.getRepo(p.cls);
            Page result = listByQ(cmdParser, p, repo);
            mv.ok(result);
        } catch (BizException e) {
            mv.error(e.getMessage());
        } catch (Throwable e) {
            logger.error("api查询异常:" + q, e);
            mv.error(e.getMessage());
        }
        return mv;
    }

    /**
     * 执行实体带fields投影的查询
     *
     * @param cmdParser
     * @param p
     * @param repo
     * @param handlers  自定义查询类
     * @return
     * @throws Exception
     */
    public static Page listByQ(CmdParser cmdParser, QueryParam p, ARepository repo, QParamHandler... handlers)
            throws Exception {
        DetachedCriteriaResult dr = cmdParser.getDetachedCriteria(p, handlers);
        DetachedCriteria dc = dr.dc;
        int page = p.start / p.limit;
        int pagesize = p.limit;
        PageRequest pr = new PageRequest(page, pagesize);
        Order[] orders = cmdParser.getOrders(p);

        Page result = null;
        if (p.fields == null || p.fields.size() == 0) {
            // 实体列表查询，不推荐（性能低下）
            result = repo.find(dc, pr, orders);
        } else {
            result = repo.findByFields(p.fields, dr, pr, orders);
        }
        return result;
    }

    /**
     * @param json exp: {name:'123123',createData:'2014-02-10 21:00:00'}
     * @return
     */
    @RequestMapping("save")
    @Transactional(readOnly = false)
    public ModelGsonView save(String cls, String json) {
        ModelGsonView mv = new ModelGsonView();
        try {
            Class clz = Class.forName(cls);
            Object m = this.getGson().fromJson(json, clz);
            this.cmdContext.getRepo(cls).save(m);
            mv.ok().add("id", ElUtils.getPK(m));
        } catch (Throwable t) {
            logger.error("保存失败", t);
            throw new BizException(t.getMessage());
        }
        return mv;
    }

    @RequestMapping("delete")
    @Transactional(readOnly = false)
    public ModelGsonView delete(String cls, Long[] id) {
        ModelGsonView mv = new ModelGsonView();
        try {
            Class clz = Class.forName(cls);
            for (Long mid : id) {
                Object m = this.cmdContext.getRepo(cls).get(mid.longValue());
                this.cmdContext.getRepo(cls).delete(m);
            }
            mv.ok();
        } catch (Throwable t) {
            logger.error("删除失败", t);
            throw new BizException(t.getMessage());
        }
        return mv;
    }

    @RequestMapping("export")
    public ModelAndView export(String q, String columns) {
        try {
            ExcelExtGrid grid = this.toExcelExtGrid(columns);
            QueryParam qp = this.getGson().fromJson(q, TYPE_QPARAM);
            ARepository repo = cmdContext.getRepo(qp.cls);
            Page pg = listByQ(cmdParser, qp, repo);
            return new ModelAndView(new ExcelExportView(grid, pg.data));
        } catch (Throwable e) {
            logger.error("api导出异常", e);
            ModelGsonView gv = new ModelGsonView();
            return gv.error(e.getMessage());
        }
    }
}
