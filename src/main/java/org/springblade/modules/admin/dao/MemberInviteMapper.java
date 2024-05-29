package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.po.MemberInvitePO;

@Mapper
public interface MemberInviteMapper extends BaseMapper<MemberInvitePO> {
}
