package org.springblade.modules.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.modules.admin.dao.MemberInviteMapper;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.pojo.po.BasePO;
import org.springblade.modules.admin.pojo.po.MemberInvitePO;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springblade.modules.admin.service.MemberConnectService;
import org.springblade.modules.admin.service.MemberInviteService;
import org.springblade.modules.admin.service.UserVSoulService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberInviteServiceImpl implements MemberInviteService {

	private final MemberInviteMapper memberInviteMapper;

	private final PFPTokenMapper pfpTokenMapper;

	private final MemberMapper memberMapper;

	private final MemberConnectService memberConnectService;

	private final UserVSoulService userVSoulService;

	private static final Integer vSoul = 20;

	/**
	 * 获取用户的邀请码列表
	 *
	 * @param userId 用户id
	 */
	@Override
	public List<MemberInvitePO> getInviteCodes(long userId) {
		//获取用户信息
		MemberPO memberPO = memberMapper.selectById(userId);
		if (ObjectUtil.isEmpty(memberPO)) {
			throw new ServiceException("该用户不存在");
		}
		// 返回长度为1的邀请码列表
		String superInviteCode = memberPO.getSuperInviteCode();
		List<MemberInvitePO> list = new ArrayList<>();
		if (superInviteCode != null) {
			MemberInvitePO memberInvitePO = new MemberInvitePO();
			memberInvitePO.setUserId(userId);
			memberInvitePO.setInviteCode(superInviteCode);
			memberInvitePO.setUsed(0);
			list.add(memberInvitePO);
		}
		return list;
//		List<MemberInvitePO> list = new LambdaQueryChainWrapper<>(memberInviteMapper).eq(MemberInvitePO::getUserId, userId).list();
//		// 判断pfpToken数量是否超出最大限制
//		Long pfpTokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//			.eq(BasePO::getIsDeleted, 0));
//		Integer mintMaxCount = Web3jConfig.MINT_MAX_COUNT;
//		boolean canUseInviteCode = pfpTokenCount < mintMaxCount;
//		// 如果pfpToken数量大于5000，使用超级邀请码作为列表返回
//		if (!canUseInviteCode) {
//			List<MemberInvitePO> superList = new ArrayList<>();
//			MemberInvitePO memberInvitePO = new MemberInvitePO();
//			memberInvitePO.setUserId(userId);
//			memberInvitePO.setInviteCode(memberPO.getSuperInviteCode());
//			memberInvitePO.setUsed(0);
//			superList.add(memberInvitePO);
//			return superList;
//		}
//
//		if (CollUtil.isEmpty(list)) { //为空，生成邀请码并返回
//			List<MemberInvitePO> newList = new ArrayList<>();
//			for (int i = 0;i < 3;i ++){
//				MemberInvitePO memberInvitePO = new MemberInvitePO();
//				memberInvitePO.setUserId(userId);
//				memberInvitePO.setInviteCode(InviteCodeGenUtil.genInviteCode(memberPO.getAddress()));
//				memberInvitePO.setUsed(0);
//				memberInvitePO.initForInsert();
//				memberInviteMapper.insert(memberInvitePO);
//				newList.add(memberInvitePO);
//			}
//			return newList;
//		}
//		return list;
	}

	/**
	 * 使用邀请码
	 *
	 * @param inviteCode 邀请码
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void useInviteCode(String inviteCode) {
		//校验邀请码
//		MemberInvitePO invitePO = new LambdaQueryChainWrapper<>(memberInviteMapper).eq(MemberInvitePO::getInviteCode, inviteCode).one();
//		// 获取pfpToken数量，以及mintMax
//		Long pfpTokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//			.eq(BasePO::getIsDeleted, 0));
//		Integer mintMaxCount = Web3jConfig.MINT_MAX_COUNT;
//		boolean canUseInviteCode = pfpTokenCount < mintMaxCount;
		Long inviteUserId = null;
		// 如果pfpToken数量不大于5000，邀请码正常使用
//		if (canUseInviteCode) {
//			if (ObjectUtil.isEmpty(invitePO)) {
//				throw new ServiceException("The invite code is incorrect");
//			}
//			if (invitePO.getUsed() == 1){
//				throw new ServiceException("The invite code is used");
//			}
//			//更新邀请码已使用
//			invitePO.setUsed(1);
//			invitePO.initForUpdate();
//			memberInviteMapper.updateById(invitePO);
//			inviteUserId = invitePO.getUserId();
//		} else {
			// 如果pfpToken数量大于5000，邀请码不可以使用，查询邀请码是否为超级邀请码
		// 改为始终使用超级邀请码
			MemberPO memberPO = memberMapper.selectOne(new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(MemberPO::getSuperInviteCode, inviteCode));
			if (ObjectUtil.isEmpty(memberPO)) {
				throw new ServiceException("The invite code is incorrect");
			}
			inviteUserId = memberPO.getId();
//		}

		//新增connect连接
		memberConnectService.addConnected(StpUtil.getLoginIdAsLong(), inviteUserId);
		//更新当前新用户的被邀请用户id字段
		MemberPO curNewUser = memberMapper.selectById(StpUtil.getLoginIdAsLong());
		if (ObjectUtil.isEmpty(curNewUser)) {
			throw new ServiceException("New user does not exist");
		}
		curNewUser.setInviteUserId(inviteUserId);
		memberMapper.updateById(curNewUser);
//		// 判断邀请人是否存在至少一个pfpToken 未挂单
//		boolean isExistPfpToken = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//			.eq(BasePO::getIsDeleted, 0)
//			// pickStatus 为0
////			.eq(PFPTokenPO::getPickStatus, 0)
//			.eq(PFPTokenPO::getOwnerUserId, inviteUserId)) > 0;
//		if (isExistPfpToken) {
//			userVSoulService.addUserVSoul(inviteUserId,new BigDecimal(String.valueOf(vSoul)), 3); //邀请人 用户id
//			// 当前用户新增20 vSoul积分
//			userVSoulService.addUserVSoul(curNewUser.getId(),new BigDecimal(String.valueOf(vSoul)), 4);
//		}
//
//		//给上上级用户新增10 vSoul积分
//		Long lastLastUserId = getLastLastUserId(inviteUserId);
//		if (ObjectUtil.isNotEmpty(lastLastUserId)){
//			// 同时判断上上级用户是否存在至少一个pfpToken未挂单
//			boolean isExistPfpToken2 = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//				.eq(BasePO::getIsDeleted, 0)
//				// pickStatus 为0
////				.eq(PFPTokenPO::getPickStatus, 0)
//				.eq(PFPTokenPO::getOwnerUserId, lastLastUserId)) > 0;
//			if (isExistPfpToken2) {
//				userVSoulService.addUserVSoul(lastLastUserId, new BigDecimal(String.valueOf(vSoul * 0.5)), 3);
//			}
//		}

//		20240122邀请人积分新逻辑
//		1. 如果没有NFT，上下两级都是基础分：2分
//		2. 如果有NFT，上下两级各得:2*booster（按照邀请人计算）
		// 获取邀请用户booster积分
		BigDecimal booster = userVSoulService.getBoostByUserId(inviteUserId);

		Integer newVSoul = 2;

		if (booster.toString().equals("1")) {
			//没有NFT，上下两级都是基础分：2分
			userVSoulService.addUserVSoul(inviteUserId,new BigDecimal(newVSoul), 3); //邀请人 用户id
			userVSoulService.addUserVSoul(curNewUser.getId(),new BigDecimal(newVSoul), 4);
		}else {
			//有NFT，上下两级各得:2*booster（按照邀请人计算）
			userVSoulService.addUserVSoul(inviteUserId,new BigDecimal(newVSoul).multiply(booster), 3); //邀请人 用户id
			userVSoulService.addUserVSoul(curNewUser.getId(),new BigDecimal(newVSoul).multiply(booster), 4);
		}
	}

	/**
	 * 得到上上级用户
	 * @param inviteUserId 邀请人用户id
	 */
	private Long getLastLastUserId(Long inviteUserId) {
		MemberPO lastUser = memberMapper.selectById(inviteUserId);
		if (ObjectUtil.isEmpty(lastUser)) {
			throw new ServiceException("Invite user does not exist");
		}
		MemberPO lastLastUser = memberMapper.selectById(lastUser.getInviteUserId());
		if (ObjectUtil.isEmpty(lastLastUser)) {
			return null;
		}else {
			return lastLastUser.getId();
		}
	}
}
