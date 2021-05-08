package org.spin.mybatis.plus;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;
import java.util.Optional;

/**
 * 分页拦截器
 * <p>
 * 默认对 left join 进行优化,虽然能优化count,但是加上分页的话如果1对多本身结果条数就是不正确的
 *
 * @author hubin
 * @since 3.4.0
 */
public class PaginationInnerInterceptor extends com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor {

    protected String autoCountSql(boolean optimizeCountSql, String sql) {
        if (!optimizeCountSql) {
            return lowLevelCountSql(sql);
        }
        try {
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Distinct distinct = plainSelect.getDistinct();
            GroupByElement groupBy = plainSelect.getGroupBy();
            List<OrderByElement> orderBy = plainSelect.getOrderByElements();

            if (CollectionUtils.isNotEmpty(orderBy)) {
                boolean canClean = groupBy == null;
                // 包含groupBy 不去除orderBy
                if (canClean) {
                    for (OrderByElement order : orderBy) {
                        // order by 里带参数,不去除order by
                        Expression expression = order.getExpression();
                        if (!(expression instanceof Column) && expression.toString().contains(StringPool.QUESTION_MARK)) {
                            canClean = false;
                            break;
                        }
                    }
                }
                if (canClean) {
                    plainSelect.setOrderByElements(null);
                }
            }
            //#95 Github, selectItems contains #{} ${}, which will be translated to ?, and it may be in a function: power(#{myInt},2)
            for (SelectItem item : plainSelect.getSelectItems()) {
                if (item.toString().contains(StringPool.QUESTION_MARK)) {
                    return lowLevelCountSql(select.toString());
                }
            }
            // 包含 distinct、groupBy不优化
            if (distinct != null || null != groupBy) {
                return lowLevelCountSql(select.toString());
            }
            // 包含 join 连表,进行判断是否移除 join 连表
            if (optimizeJoin) {
                List<Join> joins = plainSelect.getJoins();
                if (CollectionUtils.isNotEmpty(joins)) {
                    boolean canRemoveJoin = true;
                    String whereS = Optional.ofNullable(plainSelect.getWhere()).map(Expression::toString).orElse(StringPool.EMPTY);
                    // 不区分大小写
                    whereS = whereS.toLowerCase();
                    for (Join join : joins) {
                        if (!join.isLeft()) {
                            canRemoveJoin = false;
                            break;
                        }
                        FromItem rightItem = join.getRightItem();
                        String str = "";
                        if (rightItem instanceof Table) {
                            Table table = (Table) rightItem;
                            str = Optional.ofNullable(table.getAlias()).map(Alias::getName).orElse(table.getName()) + StringPool.DOT;
                        } else if (rightItem instanceof SubSelect) {
                            SubSelect subSelect = (SubSelect) rightItem;
                            /* 如果 left join 是子查询，并且子查询里包含 ?(代表有入参) 或者 where 条件里包含使用 join 的表的字段作条件,就不移除 join */
                            if (subSelect.toString().contains(StringPool.QUESTION_MARK)) {
                                canRemoveJoin = false;
                                break;
                            }
                            str = subSelect.getAlias().getName() + StringPool.DOT;
                        }
                        // 不区分大小写
                        str = str.toLowerCase();
                        String onExpressionS = join.getOnExpression().toString();
                        /* 如果 join 里包含 ?(代表有入参) 或者 where 条件里包含使用 join 的表的字段作条件,就不移除 join */
                        if (onExpressionS.contains(StringPool.QUESTION_MARK) || whereS.contains(str)) {
                            canRemoveJoin = false;
                            break;
                        }
                    }
                    if (canRemoveJoin) {
                        plainSelect.setJoins(null);
                    }
                }
            }
            // 优化 SQL
            plainSelect.setSelectItems(COUNT_SELECT_ITEM);
            return select.toString();
        } catch (JSQLParserException e) {
            // 无法优化使用原 SQL
            logger.debug("optimize this sql to a count sql has exception, sql:\"" + sql + "\", exception:\n" + e.getCause());
        } catch (Exception e) {
            logger.debug("optimize this sql to a count sql has error, sql:\"" + sql + "\", exception:\n" + e);
        }
        return lowLevelCountSql(sql);
    }

}
