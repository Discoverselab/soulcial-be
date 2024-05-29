package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.dao.PFPPickMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springblade.modules.admin.pojo.po.PFPPickPO;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;
import org.springblade.modules.admin.pojo.vo.CheckPickNftVo;
import org.springblade.modules.admin.pojo.vo.PFPPickDetailVo;
import org.springblade.modules.admin.pojo.vo.PickNftVo;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.admin.util.PickUtil;
import org.springblade.modules.system.entity.Dict;
import org.springblade.modules.system.mapper.DictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pick")
@Api(value = "NFT出价", tags = "NFT出价")
public class PickController {


	@Autowired
	MemberMapper memberMapper;

	@Autowired
	NftService nftService;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	DictMapper dictMapper;

	@Value("${contract.linkRate}")
	private BigDecimal linkRate;


	// 获取doubleVSoul的dict值
	@GetMapping("/getDoubleVSoulTime")
	@ApiOperation(value = "获取doubleVSoul的dict值")
	public R getDoubleVSoulTime() {
		Dict doubleVSoulTime = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "double_VSoul_time")
			.eq(Dict::getIsDeleted, 0));
		return R.data(doubleVSoulTime.getDictValue());
	}

	// 设置doubleVSoul的dict值
	@PostMapping("/setDoubleVSoulTime")
	@ApiOperation(value = "设置doubleVSoul的dict值")
	public R setDoubleVSoulTime(@ApiParam(value = "doubleVSoul的dict值 - 示例：2023-11-28 08:00:00", required = true) @RequestParam("doubleVSoulTime") String doubleVSoulTime) {
		Dict doubleVSoulTimeDict = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "double_VSoul_time")
			.eq(Dict::getIsDeleted, 0));
		doubleVSoulTimeDict.setDictValue(doubleVSoulTime);
		dictMapper.updateById(doubleVSoulTimeDict);
		return R.data(doubleVSoulTimeDict.getDictValue());
	}

	@GetMapping("/getPickDescription")
	@ApiOperation(value = "PICK收益描述")
	public R<List<PickUtil>> getPickDescription() {
		List<PickUtil> list = PickUtil.getList(linkRate);
		return R.data(list);
	}

	@GetMapping("/getNFTPickInfo")
	@ApiOperation(value = "getNFTPickInfo")
	public R<PFPPickDetailVo> pickNFT(@RequestParam("tokenId") Long tokenId) {
//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(tokenId);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(tokenId);
		Long pickId = pfpTokenPO.getPickId();

		PFPPickDetailVo pfpPickDetailVo = new PFPPickDetailVo();
		if (pickId != null) {
			PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);
			BeanUtil.copyProperties(pfpPickPO, pfpPickDetailVo);

			//待开奖
			if (pfpPickPO.getStatus() == 1) {
				String rewardTimeStr = "";
				//计算开奖时间
				Long diff = pfpPickPO.getRewardTime().getTime() - System.currentTimeMillis();
				if (diff < 0) {
					rewardTimeStr = "0 secs";
				} else {
					Long sec = diff / 1000;

					Long mins = sec / 60;
					Long secs = sec % 60;

					if (mins > 0) {
						if (mins == 1) {
							rewardTimeStr = mins + " min ";
						} else {
							rewardTimeStr = mins + " mins ";
						}
					}

					if (secs == 1) {
						rewardTimeStr = rewardTimeStr + secs + " sec";
					} else {
						rewardTimeStr = rewardTimeStr + secs + " secs";
					}

				}
				pfpPickDetailVo.setRewardTimeStr(rewardTimeStr);
			}

			if (pfpPickPO.getIndexUserId0() != null && pfpPickPO.getIndexUserId0() != 0) {
				MemberPO member = memberMapper.selectById(pfpPickPO.getIndexUserId0());
				pfpPickDetailVo.setIndexUserName0(member.getUserName());
				pfpPickDetailVo.setIndexAvatar0(member.getAvatar());
			}

			if (pfpPickPO.getIndexUserId1() != null && pfpPickPO.getIndexUserId1() != 0) {
				MemberPO member = memberMapper.selectById(pfpPickPO.getIndexUserId1());
				pfpPickDetailVo.setIndexUserName1(member.getUserName());
				pfpPickDetailVo.setIndexAvatar1(member.getAvatar());
			}

			if (pfpPickPO.getIndexUserId2() != null && pfpPickPO.getIndexUserId2() != 0) {
				MemberPO member = memberMapper.selectById(pfpPickPO.getIndexUserId2());
				pfpPickDetailVo.setIndexUserName2(member.getUserName());
				pfpPickDetailVo.setIndexAvatar2(member.getAvatar());
			}

			if (pfpPickPO.getIndexUserId3() != null && pfpPickPO.getIndexUserId3() != 0) {
				MemberPO member = memberMapper.selectById(pfpPickPO.getIndexUserId3());
				pfpPickDetailVo.setIndexUserName3(member.getUserName());
				pfpPickDetailVo.setIndexAvatar3(member.getAvatar());
			}

			//本人是否可以退款
			boolean isLogin = StpUtil.isLogin();
			if (isLogin) {
				Long loginIdAsLong = StpUtil.getLoginIdAsLong();
				//1.先判断当前登录用户是否pump
				Long diff = 0L;
				if (loginIdAsLong.equals(pfpPickPO.getIndexUserId0())) {
					diff = System.currentTimeMillis() - pfpPickPO.getIndexUserPickTime0().getTime();
				}
				if (loginIdAsLong.equals(pfpPickPO.getIndexUserId1())) {
					diff = System.currentTimeMillis() - pfpPickPO.getIndexUserPickTime1().getTime();
				}
				if (loginIdAsLong.equals(pfpPickPO.getIndexUserId2())) {
					diff = System.currentTimeMillis() - pfpPickPO.getIndexUserPickTime2().getTime();
				}
				if (loginIdAsLong.equals(pfpPickPO.getIndexUserId3())) {
					diff = System.currentTimeMillis() - pfpPickPO.getIndexUserPickTime3().getTime();
				}
				//2.判断pump时间是否大于7天
				if (diff > 3 * 24 * 60 * 60 * 1000) {
//				if (diff > 10 * 60 * 1000) {
					pfpPickDetailVo.setIsRefundPick("1");
				}
			}
		}
		return R.data(pfpPickDetailVo);
	}

	@PostMapping("/prePickNFT")
	@ApiOperation(value = "PickNFT前调用")
	public R prePickNFT(@Valid @RequestBody CheckPickNftVo checkPickNftVo) {
		R result = nftService.prePickNFT(checkPickNftVo);
		return result;
	}

	@PostMapping("/pickNFT")
	@ApiOperation(value = "pickNFT")
	public R pickNFT(@Valid @RequestBody PickNftVo pickNftVo) throws Exception {
		R result = nftService.pickNFT(pickNftVo);
		return result;
	}


}
