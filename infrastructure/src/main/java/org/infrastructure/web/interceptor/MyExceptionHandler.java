package org.infrastructure.web.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.exception.ConstraintViolationException;
import org.infrastructure.throwable.BizException;
import org.infrastructure.util.StringUtils;
import org.infrastructure.web.view.ModelGsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * 项目异常处理方法
 * 
 * @author zhou
 * 
 */
public class MyExceptionHandler implements HandlerExceptionResolver {
	Logger logger = LoggerFactory.getLogger(MyExceptionHandler.class);

	/**
	 * 通用异常处理
	 */
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("ex", ex);

		String requestType = request.getHeader("X-Requested-With");
		String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();

		if (contentType.indexOf("application/json") > -1 || StringUtils.isNotEmpty(requestType)) {

			ModelGsonView mv = new ModelGsonView();
			// 根据不同错误转向不同页面
			if (ex instanceof BizException) {
				logger.error("业务异常", ex);
				mv.error(ex.getMessage());
			} else if (ex instanceof HibernateOptimisticLockingFailureException) {
				mv.error("数据已被其他人修改，请重填写后保存");
			} else if (ex.getCause() != null && ex.getCause() instanceof ConstraintViolationException)
				mv.error("此数据已被业务占用，无法删除");

			else {
				if (ex != null)
					logger.error("WebReq未知异常", ex);
				mv.error(ex.getMessage());
			}
			return mv;
		}

		// 根据不同错误转向不同页面
		if (ex instanceof BizException) {
			model.put("msg", ex.getMessage());
			return new ModelAndView("common/error", model);
		} else {
			if (ex != null)
				logger.error("WebReq未知异常", ex);

			ModelAndView mv = new ModelAndView("common/error");
			mv.addObject("msg", ex.getMessage());
			return mv;
		}
	}
}
