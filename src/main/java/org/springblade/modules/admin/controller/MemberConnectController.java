package org.springblade.modules.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.pojo.vo.ConnectListVo;
import org.springblade.modules.admin.service.MemberConnectService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/connect")
@Api(value = "用户连接相关接口",tags = "用户连接相关接口")
@RequiredArgsConstructor
public class MemberConnectController {

	private final MemberConnectService memberConnectService;

	@ApiOperation(value = "发起加好友请求")
	@PutMapping("/subConnect/{toUserId}")
	public R<?> subConnect(@ApiParam(value = "被加好友的用户id",required = true) @PathVariable String toUserId) {
		memberConnectService.subConnect(toUserId);
		return R.success("发起connect请求成功");
	}

	@ApiOperation(value = "确认加好友请求")
	@PutMapping("/confirm/{connectId}")
	public R<?> confirm(@ApiParam(value = "连接请求id",required = true) @PathVariable String connectId) {
		memberConnectService.confirm(connectId);
		return R.success("确认加好友请求成功");
	}

	@ApiOperation(value = "取消好友连接")
	@PutMapping("/cancel/{connectId}")
	public R<?> cancel(@ApiParam(value = "连接请求id",required = true) @PathVariable String connectId){
		memberConnectService.cancel(connectId);
		return R.success("取消好友连接成功");
	}

	@ApiOperation(value = "好友请求列表 P1")
	@GetMapping("/list")
	public R<ConnectListVo> list(){
		ConnectListVo connectListVo = memberConnectService.list();
		return R.data(connectListVo);
	}



}
