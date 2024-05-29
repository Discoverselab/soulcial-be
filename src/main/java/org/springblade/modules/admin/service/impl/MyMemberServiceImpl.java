package org.springblade.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springblade.modules.admin.pojo.po.MemberPO;
import org.springblade.modules.admin.service.MyMemberService;
import org.springframework.stereotype.Service;

/**
 * @Auther: FengZi
 * @Date: 2024/3/25 14:50
 * @Description:
 */
@Service
public class MyMemberServiceImpl extends ServiceImpl<MemberMapper, MemberPO> implements MyMemberService {
}
