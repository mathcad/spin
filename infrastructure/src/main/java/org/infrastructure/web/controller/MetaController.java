package org.infrastructure.web.controller;

import org.infrastructure.jpa.api.CmdParser;
import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.core.ARepository;
import org.infrastructure.jpa.core.IEntity;
import org.infrastructure.jpa.core.Page;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.ElUtils;
import org.infrastructure.web.view.ExcelExportView;
import org.infrastructure.web.view.ExcelExtGrid;
import org.infrastructure.web.view.GsonView;
import org.infrastructure.web.view.ModelGsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 提供直接操控元数据的接口
 * <p>
 * 动态映射crud操作的rest full api
 */
public abstract class MetaController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(MetaController.class);

    @Autowired
    CmdParser cmdParser;

    @RequestMapping("get")
    public ModelGsonView get(@RequestParam String cls, @RequestParam Long id,
                             @RequestParam(defaultValue = "1", required = false) Integer depth,
                             @RequestParam(defaultValue = "true", required = false) boolean get) {
        ModelGsonView mv = new ModelGsonView();
        try {
            // 标记正在执行 get对象查询，枚举显示为值
            if (get)
                GsonView.setApi("get");
            ARepository<?, Long> repo = cmdContext.getRepo(cls);
            depth = depth > 2 ? 2 : depth;
            mv.ok().add("data", repo.getDto(id, depth));
        } catch (Throwable e) {
            logger.error("api查询异常", e);
            mv.error(e.getMessage());
        }
        return mv;
    }

    @RequestMapping("list")
    public ModelGsonView list(String q) {
        ModelGsonView mv = new ModelGsonView();
        try {
            if (StringUtils.isEmpty(q))
                throw new SimplifiedException("空查询无法执行");
            QueryParam p = this.getGson().fromJson(q, QueryParam.class);
            ARepository repo = cmdContext.getRepo(p.getCls());
            Page result = listByQ(cmdParser, p, repo);
            mv.ok(result);
        } catch (SimplifiedException e) {
            mv.error(e.getMessage());
        } catch (Throwable e) {
            logger.error("api查询异常:" + q, e);
            mv.error(e.getMessage());
        }
        return mv;
    }

    /**
     * @param json exp: {name:'123123',createData:'2014-02-10 21:00:00'}
     */
    @RequestMapping("save")
    @Transactional()
    public ModelGsonView save(String cls, String json) {
        ModelGsonView mv = new ModelGsonView();
        try {
            Class clz = Class.forName(cls);
            @SuppressWarnings("unchecked")
            IEntity<Long> m = (IEntity<Long>) this.getGson().fromJson(json, clz);
            this.cmdContext.getRepo(cls).save(m);
            mv.ok().add("id", ElUtils.getPK(m));
        } catch (Throwable t) {
            logger.error("保存失败", t);
            throw new SimplifiedException(t.getMessage());
        }
        return mv;
    }

    @RequestMapping("delete")
    @Transactional()
    public ModelGsonView delete(String cls, Long[] id) {
        ModelGsonView mv = new ModelGsonView();
        try {
            for (Long mid : id) {
                IEntity<Long> m = this.cmdContext.getRepo(cls).get(mid);
                this.cmdContext.getRepo(cls).delete(m);
            }
            mv.ok();
        } catch (Throwable t) {
            logger.error("删除失败", t);
            throw new SimplifiedException(t.getMessage());
        }
        return mv;
    }

    @RequestMapping("export")
    public ModelAndView export(String q, String columns) {
        try {
            ExcelExtGrid grid = this.toExcelExtGrid(columns);
            QueryParam qp = this.getGson().fromJson(q, QueryParam.class);
            ARepository repo = cmdContext.getRepo(qp.getCls());
            Page pg = listByQ(cmdParser, qp, repo);
            return new ModelAndView(new ExcelExportView(grid, pg.data));
        } catch (Throwable e) {
            logger.error("api导出异常", e);
            ModelGsonView gv = new ModelGsonView();
            return gv.error(e.getMessage());
        }
    }
}