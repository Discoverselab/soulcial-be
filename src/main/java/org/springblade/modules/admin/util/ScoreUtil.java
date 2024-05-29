package org.springblade.modules.admin.util;

import io.swagger.annotations.ApiParam;
import org.python.antlr.ast.Str;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.modules.admin.pojo.enums.SoulSocreEnum;
import org.springblade.modules.admin.pojo.vo.SoulVo;
import org.springblade.modules.admin.pojo.vo.SourceVo;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreUtil {

	public static int getMatch(String userTags,
							   Integer charisma1,
							   Integer extroversion1,
							   Integer energy1,
							   Integer wisdom1,
							   Integer art1,
							   Integer courage1,
							   String nftTags,
							   Integer charisma2,
							   Integer extroversion2,
							   Integer energy2,
							   Integer wisdom2,
							   Integer art2,
							   Integer courage2){
//		BigDecimal tagMatch = BigDecimal.ZERO;
//		if(StringUtil.isBlank(userTags) && StringUtil.isBlank(nftTags)){
//			//都为空，则为100
//			tagMatch = new BigDecimal("100");
//		} else if (StringUtil.isBlank(userTags) || StringUtil.isBlank(nftTags)){
//			//任意一个为空，则为60
//			tagMatch = new BigDecimal("60");
//		}else {
//
//			List<String> tags1 = new ArrayList<>();
//			for (String s : userTags.split(",")) {
//				tags1.add(s);
//			}
//			List<String> tags2 = new ArrayList<>();
//			for (String s : nftTags.split(",")) {
//				tags2.add(s);
//			}
//
//			//原长度
//			int oldSize = tags1.size();
//			//去掉交集
//			tags1.removeAll(tags2);
//			//交集数量
//			int intersection = oldSize - tags1.size();
//			//并集
//			tags1.addAll(tags2);
//			//并集长度
//			int union = tags1.size();
//
//			tagMatch = new BigDecimal(intersection).multiply(new BigDecimal(40)).divide(new BigDecimal(union),2,BigDecimal.ROUND_HALF_UP);
//			tagMatch = tagMatch.add(new BigDecimal(60));
//			tagMatch = tagMatch.setScale(0,BigDecimal.ROUND_HALF_UP);
//		}

		if(charisma1 == null){charisma1 = 0;}
		if(charisma2 == null){charisma2 = 0;}
		if(extroversion1 == null){extroversion1 = 0;}
		if(extroversion2 == null){extroversion2 = 0;}
		if(energy1 == null){energy1 = 0;}
		if(energy2== null){energy2= 0;}
		if(wisdom1 == null){wisdom1 = 0;}
		if(wisdom2 == null){wisdom2 = 0;}
		if(art1 == null){art1 = 0;}
		if(art2== null){art2 = 0;}
		if(courage1 == null){courage1 = 0;}
		if(courage2 == null){courage2 = 0;}



		int diff = Math.abs(charisma1 - charisma2) + Math.abs(extroversion1 - extroversion2) + Math.abs(energy1 - energy2)
			+ Math.abs(wisdom1 - wisdom2) + Math.abs(art1 - art2) + Math.abs(courage1 - courage2);

		BigDecimal divide = new BigDecimal(diff).divide(new BigDecimal("12"), 2, BigDecimal.ROUND_HALF_UP);
		BigDecimal DMatch = new BigDecimal("100").subtract(divide);
		DMatch = DMatch.setScale(0,BigDecimal.ROUND_HALF_UP);

		BigDecimal SoulMatch = DMatch.multiply(new BigDecimal(6));
//		SoulMatch = tagMatch.add(SoulMatch);
		SoulMatch =	SoulMatch.divide(new BigDecimal(6),0,BigDecimal.ROUND_HALF_UP);

		return SoulMatch.intValue();
	}


	/**
	 * 获取特征形容和身份角色
	 * @param influence  影响力
	 * @param connection  连接度
	 * @param energy      精力
	 * @param wisdom      感知
	 * @param art         艺术
	 * @param courage     勇气
	 * @return Soul
	 */
	public static SoulVo getPersonalityCharacter(int influence,int connection,int energy,int wisdom,int art,int courage){
		List<SourceVo> sourceList = new ArrayList<>();
		sourceList.add(new SourceVo("Influence",influence));
		sourceList.add(new SourceVo("Connection",connection));
		sourceList.add(new SourceVo("Energy",energy));
		sourceList.add(new SourceVo("Wisdom",wisdom));
		sourceList.add(new SourceVo("Art",art));
		sourceList.add(new SourceVo("Courage",courage));

		sourceList =  sourceList.stream().sorted(Comparator.comparing(SourceVo::getSource)).collect(Collectors.toList());

		Collections.reverse(sourceList);

		//获取第一名的分数
		SourceVo firstSourceVo = sourceList.get(0);
		//获取第二名的分数
		SourceVo secondSourceVo = sourceList.get(1);

		//获取Character
		String character;
		if (firstSourceVo.getSource() >= 24){
			character = SoulSocreEnum.getEnumByFiledName(firstSourceVo.getFiledName()).getCharacterA();
		}else {
			character = SoulSocreEnum.getEnumByFiledName(firstSourceVo.getFiledName()).getCharacterB();
		}

		//获取Personality
		String personality;
		if (secondSourceVo.getSource() >= 24){
			personality = SoulSocreEnum.getEnumByFiledName(secondSourceVo.getFiledName()).getPersonalityC();
		}else {
			personality = SoulSocreEnum.getEnumByFiledName(secondSourceVo.getFiledName()).getPersonalityD();
		}
		return new SoulVo(personality,character);
	}

}
