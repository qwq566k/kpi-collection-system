package com.baiyun.kpicollectionsystem.exception;

import com.baiyun.kpicollectionsystem.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<Void> handleValidation(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.findFirst().orElse("参数校验失败");
		return Result.failure(msg);
	}

	@ExceptionHandler(BindException.class)
	public Result<Void> handleBind(BindException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.findFirst().orElse("参数绑定失败");
		return Result.failure(msg);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
		return Result.failure("请求体格式错误");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public Result<Void> handleAccessDenied(AccessDeniedException e) {
		return Result.failure("无权访问");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
		return Result.failure(e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public Result<Void> handleOther(Exception e) {
        log.error("系统异常: {}", e.getMessage());
		return Result.failure("系统异常: 请联系管理员");
	}
}



