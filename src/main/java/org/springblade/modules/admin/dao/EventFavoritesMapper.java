package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.po.EventFavoritesPO;

@Mapper
public interface EventFavoritesMapper extends BaseMapper<EventFavoritesPO> {
}
