package com.baiyun.kpicollectionsystem.common;

import lombok.Data;

@Data
public class Result<T> {

	private Integer code;
	private String msg;
	private T data;

	public static <T> Result<T> success() {
		Result<T> r = new Result<>();
		r.setCode(1);
		r.setMsg("success");
		return r;
	}

	public static <T> Result<T> success(T data) {
		Result<T> r = success();
		r.setData(data);
		return r;
	}

	public static <T> Result<T> failure(String message) {
		Result<T> r = new Result<>();
		r.setCode(0);
		r.setMsg(message);
		return r;
	}
}



