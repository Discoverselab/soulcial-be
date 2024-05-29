package org.springblade.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.dao.ActiveMapper;
import org.springblade.modules.admin.dao.PFPPickMapper;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.dao.WhiteListMapper;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HomeServiceImpl implements HomeService {

	@Autowired
	WhiteListMapper whiteListMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	ActiveMapper activeMapper;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Override
	public boolean canMintNFT(MemberPO memberPO) {
		// 如果是white_list用户
//		WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
//			.eq(BasePO::getIsDeleted, 0)
//			.eq(WhiteListPO::getAddress, memberPO.getAddress().toLowerCase()));
//		// 获取pfp token 本人数量
//		Long pfpTokenCount = pfpTokenMapper.selectCount(new LambdaQueryWrapper<PFPTokenPO>()
//			.eq(BasePO::getIsDeleted, 0)
//			.eq(PFPTokenPO::getMintUserId, userId));
//		// 获取本人已经pick的数量
//		Long pickCount = activeMapper.selectCount(new LambdaQueryWrapper<ActivePO>()
//			.eq(BasePO::getIsDeleted, 0)
//			.eq(ActivePO::getUserAddress, memberPO.getAddress().toLowerCase()));
//
//		userInfoVo.setPickCount(pickCount.intValue());
//		int mintCount = 0; // mint阈值 1 默认 2 普通白名单 3 mint2次的白名单
//		if (whiteListPO != null){
//			// canMintTwice为1，可以mint两次
//			if (whiteListPO.getCanMintTwice() == 1) {
//				mintCount += 2;
//			} else {
//				mintCount += 1;
//			}
//		}
//		if (pickCount >= 3) {
//			mintCount += 1; // pick3次以上，可以mint机会+1
//		}
//
//		// 如果pfpTokenCount小于mintCount，可以mint
//		if (pfpTokenCount < mintCount) {
//			canMint = true;
//		}
//	}
		return false;
	}
}
