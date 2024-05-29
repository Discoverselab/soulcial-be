package org.springblade.modules.admin.pojo.enums;


public enum NFTColorEnum {

//	Random(0,"Random","{lightcoral|turquoise|light_orange|light_yellow|lavender|light_pink|skyblue|cyan}"),
	Random(0,"Random","{lightcoral|turquoise|light_yellow|lavender|light_pink|skyblue|cyan}"),

    /**
     * White
	 */
//	White(1,"White"),

	/**
	 * Red
	 */
	Red(1,"Red","lightcoral"),

	/**
	 * Red
	 */
	Pink(2,"Pink","light_pink"),

	/**
	 * Orange
	 */
//	Orange(3,"Orange","light_orange"),

	/**
	 * Yellow
	 */
	Yellow(4,"Yellow","light_yellow"),

	/**
	 * Green
	 */
	Green(5,"Green","turquoise"),

	/**
	 * Blue
	 */
	Blue(6,"Blue","skyblue"),

	/**
	 * Cyan
	 */
	Cyan(7,"Cyan","cyan"),

	/**
	 * Lavender
	 */
	Lavender(8,"Lavender","lavender"),

	/**
	 * Black
	 */
//	Black(9,"Black");
	;

	private int code;
	private String name;
	private String aigcName;

	NFTColorEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	NFTColorEnum(int code, String name, String aigcName) {
		this.code = code;
		this.name = name;
		this.aigcName = aigcName;
	}

	public static Integer getCodeByAigcName(String aigcName) {
		for (NFTColorEnum value : NFTColorEnum.values()) {
			if(value.aigcName.equalsIgnoreCase(aigcName)){
				return value.code;
			}
		}
		return null;
	}

	public static String getAigcNameByCode(Integer code) {
		for (NFTColorEnum value : NFTColorEnum.values()) {
			if(value.code == code){
				return value.aigcName;
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

	public static String getAigcNameByName(String name) {
		for (NFTColorEnum value : NFTColorEnum.values()) {
			if(value.name.equalsIgnoreCase(name)){
				return value.aigcName;
			}
		}
		return null;
	}

	public static String getNameByAigcName(String aigcName) {
		for (NFTColorEnum value : NFTColorEnum.values()) {
			if(value.aigcName.equalsIgnoreCase(aigcName)){
				return value.name;
			}
		}
		return null;
	}

	public static String getNameByCode(Integer code){
		for (NFTColorEnum value : NFTColorEnum.values()) {
			if(value.code == code){
				return value.name;
			}
		}
		return null;
	}

}
