package org.springblade.modules.admin.pojo.vo;

import jnr.ffi.annotations.In;
import lombok.Data;
import org.web3j.abi.datatypes.Int;

import java.math.BigDecimal;

@Data
public class VSoulRankVO {

	private String rank = "Unranked";

	// 用户id
	private Long userId;

	// 用户名
	private String userName;

	// 用户vSoul
	private BigDecimal vSoul;
}
