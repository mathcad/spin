package org.spin.jpa.lin;

import org.spin.jpa.lin.impl.LinqImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Selection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 语言集成查询
 */
public interface Linq<R, Q extends Linq<R, Q>> extends Lin<Linq<R, Q>, CriteriaQuery<?>> {

    Q selectAll();


    /**
     * 查询结果投影为主键设置<br>
     * 例如：<br>
     * 领域类的主键属性是id，selectId()相当于select("id")
     *
     * @return 自身
     */
    Q selectId();

    /**
     * 查询结果投影设置<br>
     * 例如：<br>
     * ...select("id", "name", ...) 等价于 select id, name, ... from ...<br>
     * 其中第一个id和name为实体类的属性名，第二个id和name为实体类对应表的字段名
     *
     * @param selections 实体属性名数组
     * @return 自身
     */
    Q select(String... selections);

    Q selectExclude(String... exclusion);

    /**
     * 查询结果投影设置
     *
     * @param selections 可以是String或者JPA标准{@link Selection}
     * @return 自身
     */
    Q select(Object... selections);

    /**
     * 查询结果投影设置
     *
     * @param selections JPA标准{@link Selection}
     * @return 自身
     */
    Q select(Selection<?>... selections);


    /**
     * 查询是否存在记录
     *
     * @return true 则存在，false 则不存在
     */
    boolean exist();

    Q exists(Class<?> domainClass);

    Q notExists(Class<?> domainClass);

    /**
     * 查询数据（永不为null）
     *
     * @return 结果集合
     */
    List<R> list();

    /**
     * 降序排列
     *
     * @param properties 实体属性名
     * @return 本身
     */
    Q desc(String... properties);

    /**
     * 降序排列
     *
     * @param expressions JPA Expression
     * @return 本身
     */
    Q desc(Expression<?>... expressions);

    /**
     * 升序排列
     *
     * @param properties 实体属性名
     * @return 本身
     */
    Q asc(String... properties);

    /**
     * 升序排列
     *
     * @param expressions JPA Expression
     * @return 本身
     */
    Q asc(Expression<?>... expressions);

    /**
     * 分页查询
     *
     * @param pageable 分页信息（包含索引页号和页的大小）
     * @return 分页结果（包含索引页号的数据和总记录数）
     */
    Page<R> paging(Pageable pageable);

    /**
     * 查询记录条数
     *
     * @return 记录条数
     */
    Long count();

    /**
     * 查询并返回一条记录（必须有一条记录）
     *
     * @return 实体对象
     */
    Optional<R> findOne();

    Optional<R> findFirst();

    /**
     * 合并重复数据
     *
     * @return 本身
     */
    Q distinct();

    /**
     * 根据投影别名转Bean（此处为查询对应的领域类）<br>
     * 注意：<br>
     * 1.此方法必须在select方法前调用<br>
     * 2.此方法后必须要调用select以提供别名依据
     *
     * @param <T> Bean泛型
     * @return 本身
     */
    <T> LinqImpl<T> aliasToBean();

    /**
     * 根据投影别名转Map<br>
     * 注意：<br>
     * 1.此方法必须在select方法前调用<br>
     * 2.此方法后必须要调用select以提供别名依据
     *
     * @param <MQ> Linq
     * @return 本身
     */
    <MQ extends Linq<Map<String, Object>, MQ>> MQ aliasToMap();

    /**
     * 根据投影别名转Tuple<br>
     * 注意：<br>
     * 1.此方法必须在select方法前调用<br>
     * 2.此方法后必须要调用select以提供别名依据
     *
     * @param <MQ> Linq
     * @return 本身
     */
    <MQ extends Linq<Tuple, MQ>> MQ aliasToTuple();

    /**
     * 根据投影别名转Bean<br>
     * 注意：<br>
     * 1.此方法必须在select方法前调用<br>
     * 2.此方法后必须要调用select以提供别名依据
     *
     * @param resultClass Bean Class
     * @param <T>         结果泛型
     * @return 本身
     */
    <T> LinqImpl<T> aliasToBean(Class<T> resultClass);

    /**
     * 分页查询，不查询记录总数
     *
     * @param pageable 分页信息（包含索引页号和页的大小）
     * @return 结果集合
     */
    List<R> list(Pageable pageable);

    /**
     * 分页查询，不查询记录总数
     *
     * @param page 分页号（从0开始）
     * @param size 分页大小
     * @return 结果集合
     */
    List<R> list(int page, int size);
}
