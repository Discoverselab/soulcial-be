package org.springblade.modules.admin.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springblade.modules.admin.cache.IUserCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时更新用户信息缓存
 * @Auther: FengZi
 * @Date: 2023/12/9 11:37
 * @Description:
 */
@Component
@Slf4j
public class UserCacheTask {


	@Scheduled(fixedDelay = 5 * 60 * 1000)
	public void sayWord() {
		log.info("=====定时任务开始执行=====");
		log.info("↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓");
		log.info("定时刷新用户信息缓存");
		//定时更新缓存信息
		IUserCache.refresh();
		log.info("↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑");
		log.info("=====定时任务执行结束=====");
	}

}
