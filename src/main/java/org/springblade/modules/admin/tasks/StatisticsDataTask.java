package org.springblade.modules.admin.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.dao.StatisticsDataMapper;
import org.springblade.modules.admin.dao.StatisticsDataNewMapper;
import org.springblade.modules.admin.pojo.po.StatisticsDataPO;
import org.springblade.modules.admin.pojo.po.StatisticsDataPONew;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 每日统计数据任务
 * @Auther: FengZi
 * @Date: 2024/1/5 18:12
 * @Description:
 */
@Component
@Slf4j
public class StatisticsDataTask {

	@Resource
	private StatisticsDataMapper statisticsDataMapper;

	@Resource
	private StatisticsDataNewMapper statisticsDataNewMapper;

	/**
	 * 每日凌晨0点过一分调度
	 * @author FengZi
	 * @date 2024/1/5 18:12
	 **/
//	@Scheduled(cron = "0 1 0 * * ?")
	public void getStatistics() {
		try {
			log.info("=====定时任务开始执行=====");
			log.info("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
			// 获取当前日期的LocalDate实例
			LocalDate today = LocalDate.now();
			// 获取当前日期前一天的日期
			LocalDate yesterday = today.minusDays(1);
			// 创建一个DateTimeFormatter格式化器，设置为YYYY-MM-DD的格式
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			// 使用格式化器将LocalDate格式化为字符串
			String formattedDate = today.format(formatter);
			StatisticsDataPO statisticsDataPO = statisticsDataMapper.selectByDateTime(formattedDate);
			log.info("查询结果：{}",statisticsDataPO);
			int insert = statisticsDataMapper.insert(statisticsDataPO);
			log.info("插入结果：{}",insert);
			log.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
			log.info("=====定时任务执行结束=====");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 每日凌晨0点过一分调度
	 * @author FengZi
	 * @date 2024/1/5 18:12
	 **/
	@Scheduled(cron = "0 1 0 * * ?")
	public void getStatistics2() {
		try {
			log.info("=====定时任务开始执行=====");
			log.info("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
			// 获取当前日期的LocalDate实例
			LocalDate today = LocalDate.now();
			// 获取当前日期前一天的日期
			LocalDate yesterday = today.minusDays(1);
			// 创建一个DateTimeFormatter格式化器，设置为YYYY-MM-DD的格式
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			// 使用格式化器将LocalDate格式化为字符串
			String formattedDate = yesterday.format(formatter);
			StatisticsDataPONew statisticsDataPONew = statisticsDataMapper.selectByDateTimeNew(formattedDate);
			log.info("查询结果：{}",statisticsDataPONew);
			int insert = statisticsDataNewMapper.insert(statisticsDataPONew);
			log.info("插入结果：{}",insert);
			log.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
			log.info("=====定时任务执行结束=====");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
