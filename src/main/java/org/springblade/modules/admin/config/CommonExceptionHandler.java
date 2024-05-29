package org.springblade.modules.admin.config;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {
	/**
	 * 拦截Exception类的异常
	 *
	 * @param e
	 * @return
	 */
	@ExceptionHandler(NotLoginException.class)
	public Object exceptionHandler(HttpServletRequest request, Exception e) {
		log.info("CommonExceptionHandler NotLoginException error:{}", e.getMessage());
		return R.fail(403, "请先登录");
	}

}

