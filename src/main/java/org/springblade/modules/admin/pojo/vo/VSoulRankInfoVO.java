package org.springblade.modules.admin.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VSoulRankInfoVO {

	// 本人price
	private BigDecimal vSoul = BigDecimal.ZERO;

	// 本人排名
	private String rank = "Unranked";

	// 排行
	private List<VSoulRankVO> rankList;
}
