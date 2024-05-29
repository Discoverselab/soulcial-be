package org.springblade.modules.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.pojo.vo.ListNFTVo;
import org.springblade.modules.admin.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/market")
@Api(value = "交易所相关接口",tags = "交易所相关接口")
@RequiredArgsConstructor
public class MarketController {

	@Autowired
	@Qualifier("newMarketService2")
	MarketService marketService;

	// listNFT
	@PostMapping("/listNFT")
	@ApiOperation(value = "listNFT")
	public R<?> listNFT(
		// 传入tokenId
		@RequestBody @Valid ListNFTVo listNFTVo
	) {
		return marketService.listNFT(listNFTVo.getId());
	}
}
