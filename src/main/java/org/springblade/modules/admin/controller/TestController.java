package org.springblade.modules.admin.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.config.ContractProperties;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.service.MarketService;
import org.springblade.modules.admin.tasks.LotteryFailureTask;
import org.springblade.modules.admin.util.StatisticsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.core.methods.response.Log;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Auther: FengZi
 * @Date: 2023/11/21 18:28
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Api(value = "测试", tags = "测试")
public class TestController {

	@Autowired
	MarketService marketService;

	@Resource
	private LotteryFailureTask lotteryFailureTask;

	@Resource
	private ContractProperties contractProperties;
	@Resource
	private PFPTokenMapper pfpTokenMapper;

	@Resource
	private StatisticsDataMapper statisticsDataMapper;

	@GetMapping("/testPickItem")
	@ApiOperation(value = "修复")
	public R test() {
		Log log = new Log();
		log.setTransactionHash("0x89f4f18f7e17a45790523af328fdccc22ee4ece136c177b15ad42f459a2cb312");
		log.setBlockNumber("112479438");
		R result = marketService.pickItem(
			"0xB39c5896A94287B9c0Bce736e505234b685c0E02",
			"0x5e4372647AF9bc11BE049B0A6c3fd9A893aCD54e",
			new BigInteger("1923"),
			new BigInteger("0"),
			log
		);
		return result;
	}

	@GetMapping("/testDealList")
	@ApiOperation("手动开奖")
	public R test(String tokinId) {
		LambdaQueryWrapper<PFPTokenPO> wp = new LambdaQueryWrapper<>();
		wp.eq(PFPTokenPO::getRealTokenId, tokinId);
		PFPTokenPO tokenPO = pfpTokenMapper.selectOne(wp);
		//判断合约地址是否新合约地址
		String contractMarketAddress = tokenPO.getContractMarketAddress();
		String marketAddress = "";
		String adminAddress = "";
		if (contractProperties.getMarketAddress().equals(contractMarketAddress)) {
			marketAddress = contractProperties.getMarketAddress();
			adminAddress = contractProperties.getAdminAddress();
		} else if (contractProperties.getNewmarketAddress().equals(contractMarketAddress)) {
			marketAddress = contractProperties.getNewmarketAddress();
			adminAddress = contractProperties.getNewadminAddress();
		} else {
			marketAddress = contractProperties.getNewmarketAddress2();
			adminAddress = contractProperties.getNewmarketAddress2();
		}
		Boolean b = lotteryFailureTask.SendDealList(tokinId, marketAddress, adminAddress);
		return R.success(String.valueOf(b));
	}

	//统计数据
	@GetMapping("/statisticsData")
	@ApiOperation("统计数据")
	public R statisticsData() {
		List<String> dateList = StatisticsUtils.getDateList("2023-11-12");
		List<StatisticsDataPONew> lists = new ArrayList<>();
		for (String s : dateList) {
			StatisticsDataPONew statisticsDataPONew = statisticsDataMapper.selectByDateTimeNew(s);
			lists.add(statisticsDataPONew);
		}
		StatisticsUtils.getVerticalData(lists);
		return R.success("统计成功");
	}


	@Resource
	private MemberConnectMapper memberConnectMapper;

	@Resource
	private ChatMemberMapper chatMemberMapper;

	@Resource
	private ChatDetailMapper chatDetailMapper;

	@Resource
	private ChatOverviewMapper chatOverviewMapper;

	//建立好友私聊
	@GetMapping("/createPrivateChat")
	@ApiOperation("建立好友私聊")
	public R createPrivateChat() {
		log.info("----脚本执行开始----");

		LambdaQueryWrapper<MemberConnectPO> wp = new LambdaQueryWrapper<>();
		wp.eq(MemberConnectPO::getStatus, 1).or().eq(MemberConnectPO::getStatus, 2);
		List<MemberConnectPO> list = memberConnectMapper.selectList(wp);
		log.info("-------->查询到具备私聊资格用户数量：{}", list.size() * 2);
		Integer count = 0;
		for (MemberConnectPO memberConnectPO : list) {
			List<Long> chats1 = chatMemberMapper.getUserChatType1(memberConnectPO.getUserId());
			List<Long> chats2 = chatMemberMapper.getUserChatType1(memberConnectPO.getToUserId());
			//判断两个list是否存在交集
			Set<Long> set1 = new HashSet<>(chats1);
			Set<Long> set2 = new HashSet<>(chats2);
			// 会将重复的值存放在set1中
			set1.retainAll(set2);
			if (set1.size() > 0) {
				log.info("用户：{} ， 用户： {} ，已存在私聊， 私聊id：{}",
					memberConnectPO.getUserId(), memberConnectPO.getToUserId(), set1.toArray());
				continue;
			}
			//创建单聊
			ChatOverviewPO chatOverviewPO = new ChatOverviewPO();
			chatOverviewPO.setType(0);
			chatOverviewPO.setTitle("Chat#Friends");
			chatOverviewPO.setCreateTime(DateUtil.date());
			chatOverviewPO.initForInsertNoAuth();
			chatOverviewMapper.insert(chatOverviewPO);
			//拉入单聊
			ChatMemberPO chatMemberPO = new ChatMemberPO();
			chatMemberPO.setUserId(memberConnectPO.getUserId());
			chatMemberPO.setChatId(chatOverviewPO.getId());
			chatMemberPO.initForInsertNoAuth();
			chatMemberMapper.insert(chatMemberPO);
			ChatMemberPO chatMemberPO2 = new ChatMemberPO();
			chatMemberPO2.setUserId(memberConnectPO.getToUserId());
			chatMemberPO2.setChatId(chatOverviewPO.getId());
			chatMemberPO2.initForInsertNoAuth();
			chatMemberMapper.insert(chatMemberPO2);

			//拉入单聊消息
			ChatDetailPO chatDetailPO = new ChatDetailPO();
			chatDetailPO.setType(99);
			chatDetailPO.setChatId(chatOverviewPO.getId());
			chatDetailPO.setUserId(memberConnectPO.getToUserId());
			chatDetailPO.setContent("joined the chat");
			chatDetailPO.initForInsertNoAuth();
			chatDetailMapper.insert(chatDetailPO);
			ChatDetailPO chatDetailPO2 = new ChatDetailPO();
			chatDetailPO2.setType(99);
			chatDetailPO2.setChatId(chatOverviewPO.getId());
			chatDetailPO2.setUserId(memberConnectPO.getUserId());
			chatDetailPO2.setContent("joined the chat");
			chatDetailPO2.initForInsertNoAuth();
			chatDetailMapper.insert(chatDetailPO2);
			log.info("用户：{} ， 用户： {} ，私聊创建成功！ 私聊id：{}",
				memberConnectPO.getUserId(), memberConnectPO.getToUserId(), chatOverviewPO.getId());
			count++;
		}
		log.info("----脚本执行成功----");
		return R.success("具备私聊资格用户数：" + list.size() + "，本次运行创建数量：" + count);
	}
}
