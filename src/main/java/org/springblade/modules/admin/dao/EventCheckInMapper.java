package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.po.EventCheckInPO;

@Mapper
public interface EventCheckInMapper extends BaseMapper<EventCheckInPO> {
}
