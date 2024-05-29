package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.modules.admin.pojo.dto.VSoulPriceTop10Dto;
import org.springblade.modules.admin.pojo.po.VSoulHistoryPO;

import java.math.BigDecimal;
import java.util.List;

/**
 * ExchangeCodePO的Dao接口
 *
 * @author
 *
 */
@Mapper
public interface VSoulHistoryMapper extends BaseMapper<VSoulHistoryPO> {

	default int insertNoZero(VSoulHistoryPO vsoulHistoryPO) {
		if (vsoulHistoryPO.getVSoulPrice().compareTo(BigDecimal.ZERO) == 0) {
			return 0;
		}
		return insert(vsoulHistoryPO);
	}

	List<VSoulPriceTop10Dto> getVSoulPriceTop10(@Param("userId")Long userId,@Param("start") String start,@Param("end") String end);

}
