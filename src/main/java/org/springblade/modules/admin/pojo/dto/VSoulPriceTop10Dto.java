package org.springblade.modules.admin.pojo.dto;


import lombok.Data;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;

@Data
public class VSoulPriceTop10Dto {

	private Long userId;

	private String userName;

	private BigDecimal totalVSoulPrice;


}
