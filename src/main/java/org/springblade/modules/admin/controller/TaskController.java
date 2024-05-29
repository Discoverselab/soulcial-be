package org.springblade.modules.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.dao.ActiveMapper;
import org.springblade.modules.admin.pojo.query.ZealyRequest;
import org.springblade.modules.admin.pojo.vo.TaskResponse;
import org.springblade.modules.admin.pojo.vo.TaskResponse2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @Auther: FengZi
 * @Date: 2024/1/9 15:31
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/api/task")
@Api(value = "任务", tags = "任务")
public class TaskController {

	public static final String FALSETASK = "{ \"error\": { \"code\": 0, \"message\": \"error message\" }, \"data\": { \"result\": false } }";
	public static final String TRUETASK = "{ \"data\": { \"result\": true } }";

	@Resource
	private ActiveMapper activeMapper;

	@GetMapping("/testtask")
	@ApiOperation(value = "测试人物")
	public String testTask(String address) {
		log.info("testTask: {}", address);
		if ("0xD3adb6c64Cddc9f08566B8AbA1129904D5b1b100".equals(address)) {
			return TRUETASK;
		} else {
			return FALSETASK;
		}
	}

	//questn平台
	@GetMapping("/pumpState")
	@ApiOperation(value = "查询该用户是否进行pump操作")
	public TaskResponse pumpState(String address) {
		log.info("pumpState address: {}", address);
		TaskResponse falseTaskResponse = TaskResponse.createResponse(false, 0, "error message");
		if (address != null && address.length() > 0) {
			Integer count = activeMapper.selectPumpCount(address);
			if (count > 0) {
				TaskResponse trueTaskResponse = TaskResponse.createResponse(true, null, null);
				return trueTaskResponse;
			} else {
				return falseTaskResponse;
			}
		} else {
			return falseTaskResponse;
		}
	}

	@GetMapping("/registrationStatus")
	@ApiOperation(value = "查询该用户是否进行pump操作")
	public TaskResponse userRegistrationStatus(String address) {
		log.info("userRegistrationStatus address: {}", address);
		TaskResponse falseTaskResponse = TaskResponse.createResponse(false, 0, "error message");
		if (address != null && address.length() > 0) {
			Integer count = activeMapper.userRegistrationStatus(address);
			if (count > 0) {
				TaskResponse trueTaskResponse = TaskResponse.createResponse(true, null, null);
				return trueTaskResponse;
			} else {
				return falseTaskResponse;
			}
		} else {
			return falseTaskResponse;
		}
	}

	@GetMapping("/hasInvitedOthers")
	@ApiOperation(value = "查询该用户是否邀请过其他用户")
	public TaskResponse hasInvitedOthers(String address) {
		log.info("hasInvitedOthers address: {}", address);
		TaskResponse falseTaskResponse = TaskResponse.createResponse(false, 0, "error message");
		if (address != null && address.length() > 0) {
			Integer count = activeMapper.hasInvitedOthers(address);
			if (count > 0) {
				TaskResponse trueTaskResponse = TaskResponse.createResponse(true, null, null);
				return trueTaskResponse;
			} else {
				return falseTaskResponse;
			}
		} else {
			return falseTaskResponse;
		}
	}


	//平台2
	@GetMapping("/isRegister")
	@ApiOperation(value = "查询该用户是否注册")
	public TaskResponse2 isRegister(String address) {
		log.info("isRegister address: {}", address);
		if (address != null && address.length() > 0) {
			Integer count = activeMapper.isRegister(address);
			if (count > 0) {
				return TaskResponse2.SUCCESS();
			} else {
				return TaskResponse2.ERROR();
			}
		} else {
			return TaskResponse2.ERROR();
		}
	}

	@GetMapping("/isPump")
	@ApiOperation(value = "查询该用户是否pump")
	public TaskResponse2 isPump(String address) {
		log.info("isPump address: {}", address);
		if (address != null && address.length() > 0) {
			Integer count = activeMapper.selectPumpCount(address);
			if (count > 0) {
				return TaskResponse2.SUCCESS();
			} else {
				return TaskResponse2.ERROR();
			}
		} else {
			return TaskResponse2.ERROR();
		}
	}

	@GetMapping("/isInvited")
	@ApiOperation(value = "查询该用户是否邀请过其他用户")
	public TaskResponse2 isInvited(String address) {
		log.info("isInvited address: {}", address);
		if (address != null && address.length() > 0) {
			Integer count = activeMapper.hasInvitedOthers(address);
			if (count > 0) {
				return TaskResponse2.SUCCESS();
			} else {
				return TaskResponse2.ERROR();
			}
		} else {
			return TaskResponse2.ERROR();
		}
	}


	//1）注册并且连接钱包
	//2）24小时内登录过并连接钱包
	//平台三
	//https://zealy.io/docs/tasks/api#creating-a-custom-endpoint
	public static final String HEADER_ADDRESS = "x-api-key";
	public static final String SUCCESS_MSG = "User completed the action";
	public static final String ERROR_MSG = "The task is not completed.";
	public static final String ERROR_MSG2 = "address is missing or empty";

	public static final String API_KEY = "";

	@PostMapping("/zealyIsRegister")
	public ResponseEntity<?> zealyIsRegister(HttpServletRequest request, @RequestBody ZealyRequest requestParam) {
		String apiKey = request.getHeader(HEADER_ADDRESS);

		String address = requestParam.getAccounts().getWallet();

		log.info("zealyIsRegister address: {}", address);
		log.info("zealyIsRegister param: {}", requestParam.toString());

		HashMap<String, String> map = new HashMap<>();

		if (address != null && !address.isEmpty()) {
			Integer count = activeMapper.isRegister(address);
			if (count > 0) {
				// 成功情况的处理，注意这里的R类型你需要替换为你自己的返回类型
				map.put("message", SUCCESS_MSG);
				return ResponseEntity.ok(map);
			} else {
				// 这里返回一个带有错误消息的R类型实例
				map.put("message", ERROR_MSG);
				return ResponseEntity.badRequest().body(map);
			}
		} else {
			// 这里也返回一个带有错误消息的R类型实例
			map.put("message", ERROR_MSG2);
			return ResponseEntity.badRequest().body(map);
		}
	}

	@PostMapping("/zealyIsLogin24h")
	public ResponseEntity<?> zealyIsLogin24h(HttpServletRequest request, @RequestBody ZealyRequest requestParam) {
		String apiKey = request.getHeader(HEADER_ADDRESS);

		String address = requestParam.getAccounts().getWallet();

		HashMap<String, String> map = new HashMap<>();

		log.info("zealyIsLogin24h address: {}", address);
		log.info("zealyIsLogin24h param: {}", requestParam.toString());

		if (address != null && !address.isEmpty()) {
			Integer count = activeMapper.isLogin24h(address);
			if (count > 0) {
				// 成功情况的处理，注意这里的R类型你需要替换为你自己的返回类型
				map.put("message", SUCCESS_MSG);
				return ResponseEntity.ok(map);
			} else {
				// 这里返回一个带有错误消息的R类型实例
				map.put("message", ERROR_MSG);
				return ResponseEntity.badRequest().body(map);
			}
		} else {
			// 这里也返回一个带有错误消息的R类型实例
			map.put("message", ERROR_MSG2);
			return ResponseEntity.badRequest().body(map);
		}
	}

	//	通过邀请码邀请了好友注册（一次性）
//	参与过pump（一次性）
//	24小时内参与过pump
	@PostMapping("/zealyIsInvited")
	@ApiOperation(value = "查询该用户是否邀请过其他用户")
	public ResponseEntity<?> zealyIsInvited(HttpServletRequest request, @RequestBody ZealyRequest requestParam) {

		String apiKey = request.getHeader(HEADER_ADDRESS);

		String address = requestParam.getAccounts().getWallet();

		HashMap<String, String> map = new HashMap<>();

		log.info("zealyIsInvited address: {}", address);
		log.info("zealyIsInvited param: {}", requestParam.toString());

		if (address != null && !address.isEmpty()) {
			Integer count = activeMapper.hasInvitedOthers(address);
			if (count > 0) {
				// 成功情况的处理，注意这里的R类型你需要替换为你自己的返回类型
				map.put("message", SUCCESS_MSG);
				return ResponseEntity.ok(map);
			} else {
				// 这里返回一个带有错误消息的R类型实例
				map.put("message", ERROR_MSG);
				return ResponseEntity.badRequest().body(map);
			}
		} else {
			// 这里也返回一个带有错误消息的R类型实例
			map.put("message", ERROR_MSG2);
			return ResponseEntity.badRequest().body(map);
		}
	}

	@PostMapping("/zealyIsPump")
	@ApiOperation(value = "查询该用户是否pump")
	public ResponseEntity<?> zealyIsPump(HttpServletRequest request, @RequestBody ZealyRequest requestParam) {

		String apiKey = request.getHeader(HEADER_ADDRESS);

		String address = requestParam.getAccounts().getWallet();

		HashMap<String, String> map = new HashMap<>();

		log.info("zealyIsPump address: {}", address);
		log.info("zealyIsPump param: {}", requestParam.toString());

		if (address != null && !address.isEmpty()) {
			Integer count = activeMapper.selectPumpCount(address);
			if (count > 0) {
				// 成功情况的处理，注意这里的R类型你需要替换为你自己的返回类型
				map.put("message", SUCCESS_MSG);
				return ResponseEntity.ok(map);
			} else {
				// 这里返回一个带有错误消息的R类型实例
				map.put("message", ERROR_MSG);
				return ResponseEntity.badRequest().body(map);
			}
		} else {
			// 这里也返回一个带有错误消息的R类型实例
			map.put("message", ERROR_MSG2);
			return ResponseEntity.badRequest().body(map);
		}
	}

	@PostMapping("/zealyIsPump24h")
	@ApiOperation(value = "查询该用户24内是否pump")
	public ResponseEntity<?> zealyIsPump24h(HttpServletRequest request, @RequestBody ZealyRequest requestParam) {

		String apiKey = request.getHeader(HEADER_ADDRESS);

		String address = requestParam.getAccounts().getWallet();

		HashMap<String, String> map = new HashMap<>();

		log.info("zealyIsPump24h address: {}", address);
		log.info("zealyIsPump24h param: {}", requestParam.toString());

		if (address != null && !address.isEmpty()) {
			Integer count = activeMapper.selectPumpCount24h(address);
			if (count > 0) {
				// 成功情况的处理，注意这里的R类型你需要替换为你自己的返回类型
				map.put("message", SUCCESS_MSG);
				return ResponseEntity.ok(map);
			} else {
				// 这里返回一个带有错误消息的R类型实例
				map.put("message", ERROR_MSG);
				return ResponseEntity.badRequest().body(map);
			}
		} else {
			// 这里也返回一个带有错误消息的R类型实例
			map.put("message", ERROR_MSG2);
			return ResponseEntity.badRequest().body(map);
		}
	}

}
