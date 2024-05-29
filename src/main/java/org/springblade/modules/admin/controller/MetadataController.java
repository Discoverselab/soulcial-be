package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.enums.*;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.vo.AddWallectHistoryVo;
import org.springblade.modules.admin.pojo.vo.WallectHistoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/metadata")
@Api(value = "metadata",tags = "metadata")
public class MetadataController {

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@GetMapping("/SoulTest/{tokenId}")
	@ApiOperation(value = "metadata")
	public String getMetadata(@PathVariable("tokenId") Long tokenId) {

		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectOne(new LambdaQueryWrapper<PFPTokenPO>().eq(PFPTokenPO::getRealTokenId, tokenId));
		if(pfpTokenPO == null){
			return "{}";
		}else {
			StringBuilder metadata = new StringBuilder();
			metadata.append("{");
			metadata.append("\"image\":\""+pfpTokenPO.getSquarePictureUrl()+"\"");
			metadata.append(",\"name\":\""+ "SoulCast #" + tokenId +"\"");
			metadata.append(",\"attributes\":[");
				metadata.append(" {\"trait_type\":\"SBTI\",\"value\":\"" + NFTLevelEnum.getNameByCode(pfpTokenPO.getLevel()) + "\"}");
//				metadata.append(",{\"trait_type\":\"Personality\",\"value\":\"" + NFTPersonalityEnum.getNameByCode(pfpTokenPO.getPersonality()) + "\"}");
				// 获取pfpToken的soul字段，分隔空格取第一个值
				String[] soul = pfpTokenPO.getSoul().split(" ");
				metadata.append(",{\"trait_type\":\"Personality\",\"value\":\"" + soul[0] + "\"}");
				metadata.append(",{\"trait_type\":\"Character\",\"value\":\"" + soul[1] + "\"}");
				metadata.append(",{\"trait_type\":\"Mood\",\"value\":\"" + NFTMoodEnum.getNameByCode(pfpTokenPO.getMood()) + "\"}");
				metadata.append(",{\"trait_type\":\"Color\",\"value\":\"" + NFTColorEnum.getNameByCode(pfpTokenPO.getColor()) + "\"}");
//			metadata.append(",{\"trait_type\":\"weather\",\"value\":\"" + NFTWeatherEnum.getNameByCode(pfpTokenPO.getWeather()) + "\"}");

				//Charisma替换成INFLUENCE
				metadata.append(",{\"trait_type\":\"Influence\",\"value\":" + pfpTokenPO.getCharisma() + ",\"max_value\":100}");
				//Extroversion替换成CONNECTION
				metadata.append(",{\"trait_type\":\"Connection\",\"value\":" + pfpTokenPO.getExtroversion() + ",\"max_value\":100}");
				metadata.append(",{\"trait_type\":\"Energy\",\"value\":" + pfpTokenPO.getEnergy() + ",\"max_value\":100}");
				metadata.append(",{\"trait_type\":\"Wisdom\",\"value\":" + pfpTokenPO.getWisdom() + ",\"max_value\":100}");
				metadata.append(",{\"trait_type\":\"Art\",\"value\":" + pfpTokenPO.getArt() + ",\"max_value\":100}");
				metadata.append(",{\"trait_type\":\"Courage\",\"value\":" + pfpTokenPO.getCourage() + ",\"max_value\":100}");
			metadata.append("]");
			metadata.append("}");
			return metadata.toString();
		}


	}

	@GetMapping("/SoulCast/{tokenId}")
	@ApiOperation(value = "metadata")
	public String getMetadata2(@PathVariable("tokenId") Long tokenId) {
		return getMetadata(tokenId);
	}
}
