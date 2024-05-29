package org.springblade.modules.admin.service.impl;

//import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.dao.PFPContractMapper;
import org.springblade.modules.admin.pojo.po.BasePO;
import org.springblade.modules.admin.pojo.po.PFPContractPO;
import org.springblade.modules.admin.pojo.vo.MintNftVo;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.admin.service.PfpContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PfpContractServiceImpl implements PfpContractService {

	@Autowired
	PFPContractMapper pfpContractMapper;

	@Value("${contract.address}")
	private String contractAddress;

	@Override
	public PFPContractPO getContract() {
		PFPContractPO pfpContractPO = pfpContractMapper.selectOne(new LambdaQueryWrapper<PFPContractPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPContractPO::getContractAddress, contractAddress));

//		PFPContractPO pfpContractPO = pfpContractMapper.selectById(2L);

		return pfpContractPO;
	}
}
