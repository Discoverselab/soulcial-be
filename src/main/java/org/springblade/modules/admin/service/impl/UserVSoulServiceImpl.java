package org.springblade.modules.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.RequiredArgsConstructor;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.dao.UserVSoulMapper;
import org.springblade.modules.admin.dao.VSoulHistoryMapper;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.service.UserVSoulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserVSoulServiceImpl implements UserVSoulService {

	private final UserVSoulMapper userVSoulMapper;

	private final VSoulHistoryMapper vSoulHistoryMapper;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Autowired
	MemberMapper memberMapper;

	/**
	 * 新增用户vSoul积分
	 *
	 * @param userId 用户id
	 * @param vSoul  积分值
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addUserVSoul(Long userId, BigDecimal vSoul, Integer type) {
		//新增用户积分
		UserVSoulPO userVSoul= new LambdaQueryChainWrapper<>(userVSoulMapper).eq(UserVSoulPO::getUserId,userId).one();
		if (ObjectUtil.isEmpty(userVSoul)) { //如果为空，则新增一条该用户积分记录
			UserVSoulPO newUserVSoul = new UserVSoulPO();
			newUserVSoul.setUserId(userId);
			newUserVSoul.setVSoulPrice(vSoul);
			newUserVSoul.initForInsert();
			userVSoulMapper.insert(newUserVSoul);
		}else { //不为空，直接更新用户积分
			userVSoul.setVSoulPrice(userVSoul.getVSoulPrice().add(vSoul));
			userVSoulMapper.updateById(userVSoul);
		}
		//同步新增用户积分记录
		VSoulHistoryPO vSoulHistoryPO = new VSoulHistoryPO();
		vSoulHistoryPO.setUserId(userId);
		vSoulHistoryPO.setType(type);
		vSoulHistoryPO.setVSoulPrice(vSoul);
		vSoulHistoryPO.initForInsert();
		vSoulHistoryMapper.insert(vSoulHistoryPO);
	}


	@Override
	public BigDecimal getBoostByUserId(Long userId) {
		// 获取当前持有NFT
		List<PFPTokenPO> pfpTokenPOS = pfpTokenMapper.selectList(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPTokenPO::getOwnerUserId, userId)
			.eq(PFPTokenPO::getMintStatus, 1));

		//如果用户没有nft返回1
		if (pfpTokenPOS.isEmpty()) {
			return new BigDecimal("1");
		}

		//NFT持有加成
		BigDecimal booster = BigDecimal.ZERO;
		for (PFPTokenPO pfpTokenPO : pfpTokenPOS) {
			Integer pfpLevel = pfpTokenPO.getLevel();
			if (pfpLevel == 1) {
				booster = booster.add(new BigDecimal("0.5"));
			} else if (pfpLevel == 2) {
				booster = booster.add(new BigDecimal("1"));
			} else if (pfpLevel == 3) {
				booster = booster.add(new BigDecimal("1.5"));
			} else if (pfpLevel == 4) {
				booster = booster.add(new BigDecimal("2"));
			} else if (pfpLevel == 5) {
				booster = booster.add(new BigDecimal("2.5"));
			}
		}

		//加成 = 最高等级nft boost+加成
		List<Integer> nftLevels = pfpTokenPOS.stream().map(s -> s.getLevel()).collect(Collectors.toList());
		Integer max = Collections.max(nftLevels);
		switch (max) {
			case 1:
				booster = booster.add(new BigDecimal("10"));
				booster = booster.subtract(new BigDecimal("0.5"));
				break;
			case 2:
				booster = booster.add(new BigDecimal("11"));
				booster = booster.subtract(new BigDecimal("1"));
				break;
			case 3:
				booster = booster.add(new BigDecimal("12"));
				booster = booster.subtract(new BigDecimal("1.5"));
				break;
			case 4:
				booster = booster.add(new BigDecimal("13"));
				booster = booster.subtract(new BigDecimal("2"));
				break;
			case 5:
				booster = booster.add(new BigDecimal("15"));
				booster = booster.subtract(new BigDecimal("2.5"));
				break;
			default:
				break;
		}

		return booster;
	}

	@Override
	public boolean setxInviteUserVSoulPriceByUserId(Long userId, BigDecimal vsoulprie) {
		//查询用户邀请人
		MemberPO memberPO = memberMapper.selectById(userId);
		if (memberPO == null){
			return false;
		}
		Long inviteUserId = memberPO.getInviteUserId();
		if (inviteUserId == null || inviteUserId == 0){
			return false;
		}
		//新增用户积分
		addUserVSoulNew(inviteUserId,vsoulprie,6);
		return true;
	}

	/**
	 * 无需登录添加用户积分
	 * @author FengZi
	 * @date 13:57 2024/1/23
	 * @param userId
	 * @param vSoul
	 * @param type
	 **/
	public void addUserVSoulNew(Long userId, BigDecimal vSoul, Integer type) {
		//新增用户积分
		UserVSoulPO userVSoul= new LambdaQueryChainWrapper<>(userVSoulMapper).eq(UserVSoulPO::getUserId,userId).one();
		if (ObjectUtil.isEmpty(userVSoul)) { //如果为空，则新增一条该用户积分记录
			UserVSoulPO newUserVSoul = new UserVSoulPO();
			newUserVSoul.setUserId(userId);
			newUserVSoul.setVSoulPrice(vSoul);
			newUserVSoul.initForInsertNoAuth();
			userVSoulMapper.insert(newUserVSoul);
		}else { //不为空，直接更新用户积分
			userVSoul.setVSoulPrice(userVSoul.getVSoulPrice().add(vSoul));
			userVSoulMapper.updateById(userVSoul);
		}
		//同步新增用户积分记录
		VSoulHistoryPO vSoulHistoryPO = new VSoulHistoryPO();
		vSoulHistoryPO.setUserId(userId);
		vSoulHistoryPO.setType(type);
		vSoulHistoryPO.setVSoulPrice(vSoul);
		vSoulHistoryPO.initForInsertNoAuth();
		vSoulHistoryMapper.insert(vSoulHistoryPO);
	}
	// updateBlockNumber

}
