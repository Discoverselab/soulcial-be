package org.springblade.modules.admin.pojo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.xmlbeans.UserType;

@Getter
@AllArgsConstructor
public enum SoulSocreEnum {

	INFLUENCE("Influence","影响力","Entertainer","Hermit","Radiant","Independent"),
	CONNECTION("Connection","连接度","Diplomat","Hermit","Outgoing","Independent"),
	Energy("Energy","精力","Activist","Hermit","Passionate","Independent"),
	Wisdom("Wisdom","感知","Citizen","Ninja","Enlightened","Humble"),
	Art("Art","艺术","Artist","Ninja","Affluent","Humble"),
	Courage("Courage","勇气","Adventurer","Ninja","Fearless","Humble")
	;

	/**
	 * 属性名称
	 */
	private final String filedName;

	/**
	 * 中文名称
	 */
	private final String chineseName;

	/**
	 * 该属性第一名时对应的身份角色
	 */
	private final String characterA;

	/**
	 * 该属性第一名时，并且低于30分对应的身份角色
	 */
	private final String characterB;

	/**
	 * 该属性第二名时 对应的特征形容
	 */
	private final String personalityC;

	/**
	 * 该属性第二名时，并且低于30分对应的特征形容
	 */
	private final String personalityD;


	public static SoulSocreEnum getEnumByFiledName(String filedName) {
		for (SoulSocreEnum scoreEnum : values()) {
			if (filedName.equals(scoreEnum.getFiledName())){
				return scoreEnum;
			}
		}
		throw new RuntimeException("'SoulSocreEnum' not found By " + filedName);
	}

}
