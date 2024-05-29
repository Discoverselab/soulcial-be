package org.springblade.modules.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.modules.admin.pojo.po.ActivePO;
import org.springblade.modules.admin.pojo.po.TxnHistoryPO;

/**
 * TxnHistoryPO的Dao接口
 *
 * @author
 *
 */
@Mapper
public interface TxnHistoryMapper extends BaseMapper<TxnHistoryPO> {
}
