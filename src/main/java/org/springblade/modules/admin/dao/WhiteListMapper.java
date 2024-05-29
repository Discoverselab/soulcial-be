package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springblade.modules.admin.pojo.po.WhiteListPO;

/**
 * ExchangeCodePO的Dao接口
 *
 * @author
 *
 */
@Mapper
public interface WhiteListMapper extends BaseMapper<WhiteListPO> {

}
