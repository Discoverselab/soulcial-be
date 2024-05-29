package org.springblade.modules.admin.pojo.vo;

import lombok.Data;

/**
 * @Auther: FengZi
 * @Date: 2024/2/7 15:30
 * @Description:
 */
@Data
public class TaskResponse2 {

	private Result result;

	@Data
	public static class Result{
		private Boolean isValid;
	}

	public static TaskResponse2 SUCCESS(){
		TaskResponse2 taskResponse2 = new TaskResponse2();
		Result result1 = new Result();
		result1.setIsValid(true);
		taskResponse2.setResult(result1);
		return taskResponse2;
	}

	public static TaskResponse2 ERROR(){
		TaskResponse2 taskResponse2 = new TaskResponse2();
		Result result1 = new Result();
		result1.setIsValid(false);
		taskResponse2.setResult(result1);
		return taskResponse2;
	}
}
