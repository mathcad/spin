package org.infrastructure.jpa.core.sqlmap;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.infrastructure.jpa.api.QueryParam;
import org.infrastructure.jpa.dto.Page;
import org.infrastructure.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * sql 执行者
 * 
 * @author zhou
 *
 */
public class SqlRepository {
	static final Logger logger = LoggerFactory.getLogger(SqlRepository.class);

	protected DataSource dataSource;

	protected NamedParameterJdbcTemplate nameJt;

	protected JdbcTemplate jt;

	@Autowired
	protected SqlLoader loader;

	/**
	 * 利用Datasource 初始化jdbcTemplate和NamedParameterJdbcTemplate
	 * 
	 * @param dataSource
	 * @version 1.0
	 */
	public void init(DataSource dataSource) {
		this.dataSource = dataSource;
		this.nameJt = new NamedParameterJdbcTemplate(dataSource);
		this.jt = new JdbcTemplate(dataSource);
	}

	/**
	 * 通过命令文件查询 + 参数Map数组（HashUtils.getMap构建参数数组）
	 * 
	 * @param cmd
	 *            命令名称
	 * @param mapParams
	 *            key1,value1,key2,value2,key3,value3 ...
	 * @return 查询结果
	 */
	public List<Map<String, Object>> findList(String cmd, Object... mapParams) {
		return findList(cmd, HashUtils.getMap(mapParams));
	}

	/**
	 * 通过命令文件查询
	 * 
	 * @param cmd
	 *            命令名称
	 * @param paramMap
	 *            参数map
	 * @return 查询结果
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> findList(String cmd, Map paramMap) {
		String sqlTxt = loader.getSql(cmd, paramMap);
		return this.nameJt.queryForList(sqlTxt, paramMap);
	}

	/**
	 * 查找单行记录
	 * 
	 * @param cmd
	 *            命令名称
	 * @param map
	 *            参数
	 * @return
	 */
	public Map<String, Object> findMap(String cmd, Map<String, ?> paramMap) {
		String sqlTxt = loader.getSql(cmd, paramMap);
		return this.nameJt.queryForMap(sqlTxt, paramMap);
	}

	/**
	 * 通过cmd命令查询总数
	 * 
	 * @param cmd
	 *            命令名称
	 * @param map
	 *            参数
	 * @return
	 */
	public Long findLong(String cmd, Map<String, ?> paramMap) {
		String sqlTxt = loader.getSql(cmd, paramMap);
		Number number = this.nameJt.queryForObject(sqlTxt, paramMap, Long.class);
		Long total = (number != null ? number.longValue() : 0);
		return total;
	}

	/**
	 * 执行update/insert命令
	 * 
	 * @param cmd
	 *            命令名
	 * @param paramMap
	 *            参数
	 * @return 影响条目
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int execute(String cmd, Map paramMap) {
		String sqlTxt = loader.getSql(cmd, paramMap);
		return this.nameJt.update(sqlTxt, paramMap);
	}

	/**
	 * 通过分页查询
	 * 
	 * @param q
	 *            QParam包含了各种元素
	 * @return 分页查询结果
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page<Map<String, Object>> findPage(String cmd, QueryParam qp) {
		String sqlTxt = loader.getSql(cmd, qp.q);
		logger.info(sqlTxt);

		String pageSqlTxt = loader.getSql("$sql.findPage",
				HashUtils.getMap("sqlTxt", sqlTxt, "start", qp.start, "limit", qp.limit));

		List<Map<String, Object>> list = nameJt.queryForList(pageSqlTxt, qp.q);

		// 增加总数统计 去除排序语句
		String totalSqlTxt = loader.getSql("$sql.findTotal", HashUtils.getMap("sqlTxt", sqlTxt));
		SqlParameterSource params = new MapSqlParameterSource(qp.q);
		Number number = this.nameJt.queryForObject(totalSqlTxt, params, Long.class);
		Long total = (number != null ? number.longValue() : 0);
		return new Page<Map<String, Object>>(list, total);
	}

	/**
	 * 批量更新执行
	 * 
	 * @param cmd
	 * @param argsMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void batchExec(String cmd, List<Map> argsMap) {
		String sqlTxt = loader.getSql(cmd, null);
		this.nameJt.batchUpdate(sqlTxt, argsMap.toArray(new Map[] {}));
	}

	/**
	 * @return the jt
	 */
	public JdbcTemplate getJt() {
		return jt;
	}

	/**
	 * @return the nameJt
	 */
	public NamedParameterJdbcTemplate getNameJt() {
		return nameJt;
	}

}
