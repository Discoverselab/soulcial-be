package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;

import java.util.List;

/**
 * ExchangeCodePO的Dao接口
 *
 * @author
 *
 */
@Mapper
public interface PFPTokenMapper extends BaseMapper<PFPTokenPO> {

	// 重写selectById，使用realTokenId进行查询
	default PFPTokenPO selectByRealTokenId(Long realTokenId) {
		return selectOne(new LambdaQueryWrapper<PFPTokenPO>()
			.eq(PFPTokenPO::getRealTokenId, realTokenId)
			.eq(PFPTokenPO::getIsDeleted, 0)
		);
	}

	List<PFPTokenPO> findDelayedLotteryTokens();
}
