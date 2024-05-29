package org.springblade.modules.admin.util;

import java.math.BigDecimal;

/**
 * 坐标位置相关util
 */
public class PositionUtil {

	/**
	 * 地球平均半径（单位：米）
	 */
	private static final double EARTH_AVG_RADIUS = 6371000;

	/**
	 * 经纬度转化为弧度(rad)
	 *
	 * @param d 经度/纬度
	 */
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}

	/**
	 * 方法三：（基于googleMap中的算法得到两经纬度之间的距离,计算精度与谷歌地图的距离精度差不多。）
	 *
	 * @param longitude1 第一点的经度
	 * @param latitude1  第一点的纬度
	 * @param longitude2 第二点的经度
	 * @param latitude2  第二点的纬度
	 * @return 返回的距离，单位m
	 */
	private static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
		double radLat1 = rad(latitude1);
		double radLat2 = rad(latitude2);
		double a = radLat1 - radLat2;
		double b = rad(longitude1) - rad(longitude2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_AVG_RADIUS;
		s = Math.round(s * 10000d) / 10000d;
		return s;
	}


	/**
	 * 计算两个位置 是否在该距离以内
	 *
	 * @param longitude1
	 * @param latitude1
	 * @param longitude2
	 * @param latitude2
	 * @param range
	 * @return boolean
	 * @author FengZi
	 * @date 18:40 2024/2/21
	 **/
	public static boolean checkWithinRange(BigDecimal longitude1, BigDecimal latitude1, BigDecimal longitude2, BigDecimal latitude2, Integer range) {
		double distance = getDistance(
			longitude1.doubleValue(),
			latitude1.doubleValue(),
			longitude2.doubleValue(),
			latitude2.doubleValue());
		double dRange = range.doubleValue();
		return dRange > distance ? true : false;
	}

	/**
	 * 计算两个位置 是否在该距离以内
	 *
	 * @param longitude1
	 * @param latitude1
	 * @param longitude2
	 * @param latitude2
	 * @param range
	 * @return boolean
	 * @author FengZi
	 * @date 18:40 2024/2/21
	 **/
	public static boolean checkWithinRange(double longitude1, double latitude1, double longitude2, double latitude2, Integer range) {
		double distance = getDistance(
			longitude1,
			latitude1,
			longitude2,
			latitude2);
		double dRange = range.doubleValue();
		return dRange > distance ? true : false;
	}

	public static void main(String[] args) {

//		30.151981, 120.077975
//		30.149304, 120.075177 在
		System.out.println(checkWithinRange(30.151981,120.077975,30.149304,120.075177,500));
//		30.145928, 120.069966 不在
		System.out.println(checkWithinRange(30.151981,120.077975,30.145928,120.069966,500));
//		30.152865, 120.079535 在
		System.out.println(checkWithinRange(30.151981,120.077975,30.152865,120.079535,500));
//		30.153775, 120.069913 不在
		System.out.println(checkWithinRange(30.151981,120.077975,30.153775,120.069913,500));

	}

}
