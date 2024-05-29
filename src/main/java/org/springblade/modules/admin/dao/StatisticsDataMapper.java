package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.po.StatisticsDataPO;
import org.springblade.modules.admin.pojo.po.StatisticsDataPONew;

/**
 * ExchangeCodePO的Dao接口
 *
 * @author
 */
@Mapper
public interface StatisticsDataMapper extends BaseMapper<StatisticsDataPO> {

	/**
	 * 根据日期查询指定日期指标数据
	 * @author FengZi
	 * @date 17:49 2024/1/5
	 * @param dateTime
	 * @return org.springblade.modules.admin.pojo.po.StatisticsDataPO
	 **/

	StatisticsDataPO selectByDateTime(String dateTime);

	/**
	 * 根据日期查询指定日期指标数据
	 * @author FengZi
	 * @date 14:36 2024/1/12
	 * @param dateTime
	 * @return org.springblade.modules.admin.pojo.po.StatisticsDataPONew
	 **/
	StatisticsDataPONew selectByDateTimeNew(String dateTime);

}
