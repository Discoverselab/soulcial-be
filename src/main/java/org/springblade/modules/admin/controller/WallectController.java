package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.dto.VSoulPriceTop10Dto;
import org.springblade.modules.admin.pojo.po.BasePO;
import org.springblade.modules.admin.pojo.po.UserVSoulPO;
import org.springblade.modules.admin.pojo.po.VSoulHistoryPO;
import org.springblade.modules.admin.pojo.po.WallectHistoryPO;
import org.springblade.modules.admin.pojo.vo.AddWallectHistoryVo;
import org.springblade.modules.admin.pojo.vo.VSoulRankInfoVO;
import org.springblade.modules.admin.pojo.vo.VSoulRankVO;
import org.springblade.modules.admin.pojo.vo.WallectHistoryVo;
import org.springblade.modules.admin.service.UserVSoulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/wallect")
@Api(value = "钱包",tags = "钱包")
public class WallectController {


	@Autowired
	MemberMapper memberMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	WallectHistoryMapper wallectHistoryMapper;

	@Autowired
	UserVSoulMapper userVSoulMapper;

	@Autowired
	VSoulHistoryMapper vSoulHistoryMapper;

	@Autowired
	UserVSoulService userVSoulService;

	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


	@GetMapping("/getWallectHistory")
	@ApiOperation(value = "钱包记录")
	public R<IPage<WallectHistoryVo>> getWallectHistory(@RequestParam(value = "pageNum",required = false)  Integer pageNum,
													   @RequestParam(value = "pageSize",required = false) Integer pageSize) {
		if(pageNum == null){
			pageNum = 1;
		}
		if(pageSize == null){
			pageSize = 10;
		}

		Long userId = StpUtil.getLoginIdAsLong();

		Page<WallectHistoryPO> page = new Page<>(pageNum,pageSize);
		page = wallectHistoryMapper.selectPage(page,new LambdaQueryWrapper<WallectHistoryPO>()
			.eq(WallectHistoryPO::getUserId, userId)
			.eq(BasePO::getIsDeleted, 0)
			.orderByDesc(BasePO::getCreateTime));

		IPage<WallectHistoryVo> result = page.convert(x -> {
			WallectHistoryVo vo = new WallectHistoryVo();
			BeanUtil.copy(x, vo);
			return vo;
		});

		return R.data(result);
	}

	@PostMapping("/addWallectHistory")
	@ApiOperation(value = "添加钱包记录")
	public R<List<WallectHistoryVo>> addWallectHistory(@Valid @RequestBody AddWallectHistoryVo addWallectHistoryVo) {

		Integer type = addWallectHistoryVo.getType();
		if(type != 0 && type != 1){
			return R.fail("type must be 0 or 1");
		}

		Long userId = StpUtil.getLoginIdAsLong();
		WallectHistoryPO wallectHistoryPO = new WallectHistoryPO();
		wallectHistoryPO.setUserId(userId);
		wallectHistoryPO.setType(type);
		wallectHistoryPO.setTxnHash(addWallectHistoryVo.getTxnHash());
		wallectHistoryPO.setPrice(addWallectHistoryVo.getPrice());
		wallectHistoryPO.initForInsert();

		wallectHistoryMapper.insert(wallectHistoryPO);
		return R.success("add success");
	}

	@GetMapping("/getVSoulBalance")
	@ApiOperation(value = "获取用户积分余额")
	public R<UserVSoulPO> getVSoulBalance() {

		Long userId = StpUtil.getLoginIdAsLong();

		UserVSoulPO userVSoulPO = userVSoulMapper.selectOne(new LambdaQueryWrapper<UserVSoulPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(UserVSoulPO::getUserId, userId));

		if(userVSoulPO == null){
			userVSoulPO = new UserVSoulPO();
			userVSoulPO.setUserId(userId);
			userVSoulPO.setVSoulPrice(BigDecimal.ZERO);
			userVSoulPO.initForInsertNoAuth();

			userVSoulMapper.insert(userVSoulPO);
		}

		//获取用户Booster值
		BigDecimal Booster = userVSoulService.getBoostByUserId(userId);
		userVSoulPO.setBooster(Booster);


		return R.data(userVSoulPO);
	}

	@GetMapping("/getVSoulRank")
	@ApiOperation(value = "获取积分排行榜")
	public R<VSoulRankInfoVO> getVSoulRank(
		@RequestParam(value = "requestThisMonday",required = false) String requestThisMonday,
		@RequestParam(value = "requestNextMonday",required = false) String requestNextMonday) {

		Long userId = StpUtil.getLoginIdAsLong();

		VSoulRankInfoVO list = new VSoulRankInfoVO();

		//获取本周一日期以及下周一日期
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime thisMonday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(7).withMinute(59).withSecond(59);
		LocalDateTime nextMonday;

		// 如果当前时间是周一并且早于7点59分59秒，那么本周的时间将会设为上周周一的7点59分59秒
		if (now.getDayOfWeek() == DayOfWeek.MONDAY && now.toLocalTime().isBefore(LocalTime.of(7, 59, 59))) {
			thisMonday = thisMonday.minusWeeks(1);
		}
		// 下一周的周一7点59分59秒
		nextMonday = thisMonday.plusWeeks(1);
		String thisMondayString = dateFormatter.format(thisMonday);
		String nextMondayString = dateFormatter.format(nextMonday);
		if (requestNextMonday != null && requestNextMonday.length() > 0) {
			nextMondayString = requestNextMonday;
		}
		if (requestThisMonday != null && requestThisMonday.length() > 0) {
			thisMondayString = requestThisMonday;
		}
		List<VSoulPriceTop10Dto> vSoulPriceTop10 = vSoulHistoryMapper.getVSoulPriceTop10(userId,thisMondayString,nextMondayString);

		VSoulPriceTop10Dto selfInfo = vSoulPriceTop10.get(0);

		if (selfInfo != null) {
			list.setVSoul(selfInfo.getTotalVSoulPrice());
		}

		AtomicInteger atomicInteger = new AtomicInteger();
		List<VSoulRankVO> rankList = vSoulPriceTop10.stream().map(s -> {
			// 从第二个开始
			if (atomicInteger.get() == 0) {
				atomicInteger.incrementAndGet();
				return null;
			}
			VSoulRankVO vSoulRankVO = new VSoulRankVO();
			vSoulRankVO.setVSoul(s.getTotalVSoulPrice());
			vSoulRankVO.setUserId(s.getUserId());
			vSoulRankVO.setUserName(s.getUserName());
			vSoulRankVO.setRank(String.valueOf(atomicInteger.get()));
			if (Objects.equals(s.getUserId(), userId)) {
				list.setRank(String.valueOf(atomicInteger.get()));
			}
			atomicInteger.incrementAndGet();

			return vSoulRankVO;
		}).filter(Objects::nonNull).collect(Collectors.toList());
		list.setRankList(rankList);


		return R.data(list);
	}

	@GetMapping("/getVSoulHistory")
	@ApiOperation(value = "积分历史记录 P1")
	public R<Page<VSoulHistoryPO>> getVSoulHistory(@RequestParam(value = "pageNum",required = false)  Integer pageNum,
												   @RequestParam(value = "pageSize",required = false) Integer pageSize) {
		if(pageNum == null){
			pageNum = 1;
		}
		if(pageSize == null){
			pageSize = 10;
		}

		Long userId = StpUtil.getLoginIdAsLong();

		Page<VSoulHistoryPO> page = new Page<>(pageNum,pageSize);
		page = vSoulHistoryMapper.selectPage(page, new LambdaQueryWrapper<VSoulHistoryPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(VSoulHistoryPO::getUserId, userId)
			.orderByDesc(BasePO::getCreateTime));

		return R.data(page);
	}


	@GetMapping("/getCurrentUserEarnings")
	@ApiOperation(value = "获取 Creartor Earnings ，Compensation ，referral reward的总金额")
	public R getCurrentUserEarnings() {
		Long loginIdAsLong = StpUtil.getLoginIdAsLong();
		if (loginIdAsLong == null || loginIdAsLong == 0) {
			return R.data("0");
		}
		String currentUserEarnings = memberMapper.getCurrentUserEarnings(loginIdAsLong);
		if (currentUserEarnings == null) {
			currentUserEarnings = "0";
		}
		return R.data(currentUserEarnings);
	}

}
