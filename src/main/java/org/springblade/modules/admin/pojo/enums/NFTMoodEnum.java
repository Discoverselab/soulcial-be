package org.springblade.modules.admin.pojo.enums;


import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.admin.pojo.vo.EnumVo;

public enum NFTMoodEnum {

	Random(0,"Random","{Excited|Angry|Shocked|smile|embarrass|sad|scared}"),

    /**
     * Excited
	 */
	Excited(1,"Excited","Excited"),

	/**
	 * Calm
	 */
//	Calm(2,"Calm"),

	/**
	 * Angry
	 */
	Angry(3,"Angry","Angry"),

	/**
	 * Shocked
	 */
	Shocked(4,"Shocked","Shocked"),

	/**
	 * Cheerful
	 */
	Cheerful(5,"Cheerful","smile"),

	/**
	 * Confused
	 */
	Confused(6,"Confused","embarrass"),

	/**
	 * Heartbroken
	 */
	Heartbroken(7,"Heartbroken","sad"),

	/**
	 * Fearful
	 */
	Fearful(8,"Fearful","scared");


	private int code;
	private String name;

	private String aigcName;

	NFTMoodEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	NFTMoodEnum(int code, String name, String aigcName) {
		this.code = code;
		this.name = name;
		this.aigcName = aigcName;
	}

	public static String getAigcNameByName(String name) {
		for (NFTMoodEnum value : NFTMoodEnum.values()) {
			if(value.name.equalsIgnoreCase(name)){
				return value.aigcName;
			}
		}
		return null;
	}

	public static String getAigcNameByCode(Integer code) {
		for (NFTMoodEnum value : NFTMoodEnum.values()) {
			if(value.code == code){
				return value.aigcName;
			}
		}
		return null;
	}

	public static Integer getCodeByAigcName(String aigcName) {
		for (NFTMoodEnum value : NFTMoodEnum.values()) {
			if(value.aigcName.equalsIgnoreCase(aigcName)){
				return value.code;
			}
		}
		return null;
	}

	public static String getNameByAigcName(String aigcName) {
		for (NFTMoodEnum value : NFTMoodEnum.values()) {
			if(value.aigcName.equalsIgnoreCase(aigcName)){
				return value.name;
			}
		}
		return null;
	}

	public int getCode() {
		return code;
	};

	public String getName() {
		return name;
	};

	public static String getNameByCode(Integer code){
		for (NFTMoodEnum value : NFTMoodEnum.values()) {
			if(value.code == code){
				return value.name;
			}
		}
		return null;
	}

}
