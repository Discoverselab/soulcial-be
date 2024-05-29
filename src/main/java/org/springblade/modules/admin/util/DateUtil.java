package org.springblade.modules.admin.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 * @Auther: FengZi
 * @Date: 2024/2/21 18:32
 * @Description:
 */
public class DateUtil {

	/**
	 * 检查当前时间是否在指定的开始时间和结束时间之间（包括开始和结束时间）
	 *
	 * @param startDate 开始时间（Date类型，包含日期和时间）
	 * @param endDate 结束时间（Date类型，包含日期和时间）
	 * @return 如果当前时间在指定时间段内，则返回true；否则返回false
	 */
	public static boolean isTimeInPeriod(Date startDate, Date endDate) {
		// 将java.util.Date转换为java.time.LocalTime
		LocalTime startTime = convertDateToLocalTime(startDate);
		LocalTime endTime = convertDateToLocalTime(endDate);

		// 获取当前时间
		LocalTime currentTime = LocalDateTime.now().toLocalTime();

		// 如果开始时间小于结束时间，则直接比较
		if (startTime.isBefore(endTime)) {
			return (!currentTime.isBefore(startTime) && currentTime.isBefore(endTime)) || currentTime.equals(startTime) || currentTime.equals(endTime);
		}

		// 如果开始时间大于结束时间（跨日情况），则需要分别比较
		// 但由于我们是从Date转换过来的，跨日情况应该由调用者处理，因此这里不处理跨日
		// 如果确实需要处理跨日，则应该在传入参数时就已经考虑好日期的连续性
		return false;
	}

	/**
	 * 检查当前日期是否在指定的开始日期和结束日期之间（包括开始和结束日期）
	 *
	 * @param startDate 开始日期（Date类型）
	 * @param endDate 结束日期（Date类型）
	 * @return 如果当前日期在指定日期段内，则返回true；否则返回false
	 */
	public static boolean isDateInPeriod(Date startDate, Date endDate) {
		// 将java.util.Date转换为java.time.LocalDate
		LocalDate startLocalDate = convertDateToLocalDate(startDate);
		LocalDate endLocalDate = convertDateToLocalDate(endDate);

		// 获取当前日期
		LocalDate currentLocalDate = LocalDate.now();

		// 判断当前日期是否在开始日期和结束日期之间（包括开始和结束日期）
		return (!currentLocalDate.isBefore(startLocalDate) && currentLocalDate.isBefore(endLocalDate.plusDays(1)))
			|| currentLocalDate.equals(startLocalDate)
			|| currentLocalDate.equals(endLocalDate);
	}



	/**
	 * 将java.util.Date转换为java.time.LocalTime
	 *
	 * @param date 要转换的Date对象
	 * @return 转换后的LocalTime对象
	 */
	private static LocalTime convertDateToLocalTime(Date date) {
		Instant instant = date.toInstant();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		return localDateTime.toLocalTime();
	}

	/**
	 * 将java.util.Date转换为java.time.LocalDate
	 *
	 * @param date 要转换的Date对象
	 * @return 转换后的LocalDate对象
	 */
	private static LocalDate convertDateToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}


	/**
	 * str转date 格式为 yyyy-MM-dd HH:mm
	 * @author FengZi
	 * @date 10:20 2024/2/22
	 * @param strDate
	 * @return java.util.Date
	 **/
	public static Date getStrToDate(String strDate) {
		if (strDate == null || strDate.isEmpty()){
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

		LocalDateTime dateTime = LocalDateTime.parse(strDate, formatter);
//		System.out.println("转换后的日期时间: " + dateTime);

		// 如果你确实需要java.util.Date类型，你可以这样转换：
		Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
//		System.out.println("转换后的日期: " + date);
		return date;
	}

	public static void main(String[] args) {
		// 示例用法，你需要根据实际情况创建startDate和endDate
		Date startDate = new Date(); // 假设这是今天的某个时间点
		Date startDate2 = new Date(startDate.getTime() + 3200 * 1000); // 假设这是今天的某个时间点
		// 注意：endDate需要大于startDate，否则在上面的方法中将返回false（不处理跨日情况）
		Date endDate = new Date(startDate.getTime() + 3600 * 1000); // 假设这是开始时间后一小时的时间点

		boolean isInPeriod = isTimeInPeriod(startDate2, endDate);
		System.out.println("当前时间是否在指定时间段内：" + isInPeriod);
	}

}

