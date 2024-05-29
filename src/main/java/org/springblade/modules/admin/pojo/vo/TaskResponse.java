package org.springblade.modules.admin.pojo.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {

	private Error error;
	private Data data;

	// 适当的构造器、getter和setter方法

	@lombok.Data
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Error {
		private int code;
		private String message;

		// 构造器、getter和setter方法
	}

	@lombok.Data
	// Data类总是包含result，所以不需要@JsonInclude注解
	public static class Data {
		private boolean result;

		// 构造器、getter和setter方法
	}

	// 快速创建任意响应的静态方法
	public static TaskResponse createResponse(Boolean isSuccess, Integer errorCode, String errorMessage) {
		TaskResponse response = new TaskResponse();
		Data data = new Data();
		data.setResult(isSuccess);
		response.setData(data);

		if (!isSuccess && errorCode != null && errorMessage != null) {
			Error error = new Error();
			error.setCode(errorCode);
			error.setMessage(errorMessage);
			response.setError(error);
		}

		return response;
	}

	// 快速创建任意响应的静态方法
	public static String createResponseJSON(Boolean isSuccess, Integer errorCode, String errorMessage) {
		TaskResponse response = new TaskResponse();
		Data data = new Data();
		data.setResult(isSuccess);
		response.setData(data);

		if (!isSuccess && errorCode != null && errorMessage != null) {
			Error error = new Error();
			error.setCode(errorCode);
			error.setMessage(errorMessage);
			response.setError(error);
		}
		ObjectMapper objectMapper = new ObjectMapper();

		String taskJson = null;
		try {
			taskJson = objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			log.error("createResponseJSON error: {}", e);
		}
		return taskJson;
	}

	public static void main(String[] args) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		// 错误任务示例
		TaskResponse falseTaskResponse = TaskResponse.createResponse(false, 0, "error message");
//		String falseTaskJson = objectMapper.writeValueAsString(falseTaskResponse);
		System.out.println("False Task JSON: " + falseTaskResponse);

		// 成功任务示例
		TaskResponse trueTaskResponse = TaskResponse.createResponse(true, null, null);
//		String trueTaskJson = objectMapper.writeValueAsString(trueTaskResponse);
		System.out.println("True Task JSON: " + trueTaskResponse);
	}
}
