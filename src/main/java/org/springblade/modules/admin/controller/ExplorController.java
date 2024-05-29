package org.springblade.modules.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.modules.admin.cache.IUserCache;
import org.springblade.modules.admin.config.ContractProperties;
import org.springblade.modules.admin.dao.*;
import org.springblade.modules.admin.pojo.dto.IUserCacheDto;
import org.springblade.modules.admin.pojo.po.*;
import org.springblade.modules.admin.pojo.query.ActivePageQuery;
import org.springblade.modules.admin.pojo.vo.PFPTokenDetailVo;
import org.springblade.modules.admin.pojo.vo.PFPTokenPageVo;
import org.springblade.modules.admin.service.NftService;
import org.springblade.modules.admin.util.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/explor")
@Api(value = "NFT列表相关接口(Explor)", tags = "NFT列表相关接口(Explor)")
public class ExplorController {

	@Autowired
	MemberMapper memberMapper;

	@Autowired
	NftService nftService;

	@Autowired
	PFPTokenMapper pfpTokenMapper;

	@Autowired
	PFPHistoryMapper pfpHistoryMapper;

	@Autowired
	ActiveMapper activeMapper;

	@Autowired
	PFPPickMapper pfpPickMapper;

	@Autowired
	MemberConnectMapper memberConnectMapper;

	// 交易所合约地址
	@Value("${contract.newmarketAddress2}")
	private String marketAddress;


	@Resource
	private ContractProperties contractProperties;

	@Resource
	private EventMapper eventMapper;

	// 首页搜索功能
	@GetMapping("/indexSearch")
	@ApiOperation(value = "首页搜索功能")
	public R indexSearch(@ApiParam(value = "搜索关键字", required = true) @RequestParam("keyword") String keyword,
						 // type - 0 nft  1  member
						 @ApiParam(value = "搜索类型 0 NFT 1 用户", required = true) @RequestParam("type") int type,
						 @ApiParam(value = "当前页", required = true) @RequestParam("current") Integer current,
						 @ApiParam(value = "每页的数量", required = true) @RequestParam("size") Integer size
	) {

		if (type == 0) {
			Page<PFPTokenPO> page = new Page<>(current, size);
			LambdaQueryWrapper<PFPTokenPO> wrapper = new LambdaQueryWrapper<PFPTokenPO>()
				.eq(BasePO::getIsDeleted, 0)
				.eq(PFPTokenPO::getMintStatus, 1)
				.like(PFPTokenPO::getMintUserAddress, keyword)
				.or()
				.like(PFPTokenPO::getOwnerAddress, keyword)
				.or()
				.like(PFPTokenPO::getSoul, keyword)
				.or()
				.eq(PFPTokenPO::getRealTokenId, keyword)
				// 时间倒序
				.orderByDesc(PFPTokenPO::getUpdateTime);
			page = pfpTokenMapper.selectPage(page, wrapper);
			Page<PFPTokenPageVo> pfpTokenPageVo = new Page<>(current, size);
			BeanUtil.copyProperties(page, pfpTokenPageVo);
			List<PFPTokenPageVo> pfpTokenPageVos = new ArrayList<>();
			if (page.getRecords() != null && page.getRecords().size() > 0) {
				pfpTokenPageVos = BeanUtil.copyProperties(page.getRecords(), PFPTokenPageVo.class);
			}
			pfpTokenPageVo.setRecords(pfpTokenPageVos);
			return R.data(pfpTokenPageVo);
		} else if (type == 1) {
			Page<MemberPO> page = new Page<>(current, size);
			LambdaQueryWrapper<MemberPO> wrapper = new LambdaQueryWrapper<MemberPO>()
				.eq(BasePO::getIsDeleted, 0)
				.like(MemberPO::getUserName, keyword)
				.or()
				.like(MemberPO::getAddress, keyword)
				// 时间倒序
				.orderByDesc(MemberPO::getUpdateTime);
			page = memberMapper.selectPage(page, wrapper);
			return R.data(page);
		}
		return R.fail("type error");
	}

//	@GetMapping("/getActivePage")
//	@ApiOperation(value = "获取平台动态分页")
//	public R<Page<ActivePO>> getActivePage(
//		@ApiParam(value = "当前页",required = true) @RequestParam("current")Integer current,
//		@ApiParam(value = "每页的数量",required = true) @RequestParam("size")Integer size
//	) {
//		// 分页查询，left join member表, 根据时间倒序，并将member表中的userName和avatar字段查询出来赋值给activePO得username和userImg字段
//		Page<ActivePO> page = new Page<>(current,size);
//		LambdaQueryWrapper<ActivePO> wrapper = new LambdaQueryWrapper<ActivePO>()
//			.eq(BasePO::getIsDeleted, 0)
//			.orderByDesc(BasePO::getCreateTime);
//		page = activeMapper.selectPage(page, wrapper);
//
//		// 将member表中的userName和avatar字段查询出来赋值给activePO得username和userImg字段
//		List<ActivePO> activePOS = page.getRecords();
//		List<String> usersAddress = activePOS.stream().map(ActivePO::getUserAddress).collect(Collectors.toList());
//		List<MemberPO> memberPOS = memberMapper.selectList(new LambdaQueryWrapper<MemberPO>()
//			.in(MemberPO::getAddress, usersAddress));
//		page.setRecords(activePOS.stream().map(activePO -> {
//			memberPOS.forEach(memberPO -> {
//				if (memberPO.getAddress().equals(activePO.getUserAddress())) {
//					activePO.setUsername(memberPO.getUserName());
//					activePO.setUserImg(memberPO.getAvatar());
//				}
//			});
//			return activePO;
//		}).collect(Collectors.toList()));
//
//		return R.data(page);
//	}


	@GetMapping("/getActivePage")
	@ApiOperation(value = "获取平台动态分页")
	public R<IPage<ActivePO>> getActivePage(
		ActivePageQuery query
	) {
		Page<ActivePO> page = new Page<>(query.getCurrent(), query.getSize());

		IPage<ActivePO> activePOIPage = activeMapper.selectPageVo(page, query.getPickStatus());

		List<Long> tokenId = activePOIPage.getRecords().stream().map(ActivePO::getTokenId).distinct().filter(s -> s != null).collect(Collectors.toList());


		Map<Long, PFPTokenPO> tokenMap;
		if (tokenId.size() > 0) {
			LambdaQueryWrapper<PFPTokenPO> wp = new LambdaQueryWrapper<>();
			wp.in(PFPTokenPO::getRealTokenId, tokenId);
			tokenMap = pfpTokenMapper.selectList(wp).stream()
				.collect(Collectors.toMap(PFPTokenPO::getRealTokenId, x -> x));
		} else {
			tokenMap = new HashMap<>();
		}

		List<ActivePO> activePOS = page.getRecords();

		List<Long> eventIds = activePOIPage.getRecords().stream().map(ActivePO::getEventId).filter(s -> s != null).distinct().collect(Collectors.toList());

		Map<Long, EventPO> eventMp;
		if (eventIds != null && eventIds.size() > 0) {
			LambdaQueryWrapper<EventPO> eventWp = new LambdaQueryWrapper<>();
			eventWp.in(EventPO::getId, eventIds);
			eventMp = eventMapper.selectList(eventWp).stream()
				.collect(Collectors.toMap(EventPO::getId, x -> x));
		} else {
			eventMp = null;
		}


		page.setRecords(activePOS.stream().map(activePO -> {
			if (activePO.getType() == 3) {
				//如果是开奖通知 直接从tokenUserId字段获取token的铸造者即可
				IUserCacheDto userCache = IUserCache.getUserCache(activePO.getTokenUserId());
				if (userCache != null) {
					activePO.setTokenUserName(userCache.getUserName());
				}
				//中奖用户
				IUserCacheDto lotteryUser = IUserCache.getUserCache(activePO.getLotteryUserId());
				if (lotteryUser != null) {
					activePO.setUsername(lotteryUser.getUserName());
					activePO.setUserImg(lotteryUser.getUserAvatar());
					activePO.setUserAddress(lotteryUser.getAddress());
				}
			} else if (activePO.getType() == 4) {
				//活动签到通知
				EventPO eventPO = eventMp.get(activePO.getEventId());
				if (eventPO != null) {
					activePO.setEventName(eventPO.getEventName());
					activePO.setEventBannerUrl(eventPO.getEventBanner());
				}
				//签到用户
				IUserCacheDto eventUser = IUserCache.getUserCache(activePO.getEventUserId());
				if (eventUser != null) {
					activePO.setUsername(eventUser.getUserName());
					activePO.setUserImg(eventUser.getUserAvatar());
					activePO.setUserAddress(eventUser.getAddress());
				}
			} else {
				//为其他通知根据tokenid从token表中中获取token的铸造者
				PFPTokenPO tokenPO = tokenMap.get(activePO.getTokenId());
				if (tokenPO != null) {
					Long mintUserId = tokenPO.getMintUserId();
					IUserCacheDto mintUser = IUserCache.getUserCache(mintUserId);
					if (mintUser != null) {
						activePO.setTokenUserName(mintUser.getUserName());
					}
				}
				//获取用户信息 正常通知
				IUserCacheDto userData = IUserCache.getUserCacheByAddress(activePO.getUserAddress());
				if (userData != null) {
					activePO.setUsername(userData.getUserName());
					activePO.setUserImg(userData.getUserAvatar());
				}
			}
			return activePO;
		}).collect(Collectors.toList()));

		return R.data(activePOIPage);
	}

	@GetMapping("/getNFTPage")
	@ApiOperation(value = "获取NFT分页 P1")
	public R<Page<PFPTokenPageVo>> getNFTPage(@ApiParam(value = "排序字段：0-level 1-match 2-price 3-likes 4-listTime(默认)", required = true) @RequestParam("orderColumn") Integer orderColumn,
											  @ApiParam(value = "排序方式：0-降序(默认) 1-升序", required = true) @RequestParam("orderType") Integer orderType,
											  // pickStatus
											  @ApiParam(value = "pickStatus = 0为隐藏版，1为非隐藏版", required = true) @RequestParam("pickStatus") Integer pickStatus,
											  @ApiParam(value = "pageType = 0为默认 1为connected", required = false) @RequestParam(value = "pageType", required = false) Integer pageType,
											  @ApiParam(value = "当前页", required = true) @RequestParam("current") Integer current,
											  @ApiParam(value = "每页的数量", required = true) @RequestParam("size") Integer size) {
		boolean isLogin = StpUtil.isLogin();
		if (!isLogin && (orderColumn == 1 || orderType == 1)) {
			//如果未登录，并且选了match排序 或者connected
			return R.fail("please login first");
		}

		Page<PFPTokenPO> page = new Page<>(current, size);
		LambdaQueryWrapper<PFPTokenPO> wrapper = new LambdaQueryWrapper<>();

		// ------------------2023-11-28 重写默认排序，后续继续重构首页查询逻辑------------------
		if (orderColumn == 4 && pageType != 1) {
			// 默认排序
			// 按rank查询，取前15条记录
			Page<PFPTokenPO> pageRank = new Page<>(current, size - 5);  // 指定查询第1页，每页15条
			LambdaQueryWrapper<PFPTokenPO> wrapperRank = new LambdaQueryWrapper<>();
			wrapperRank.orderByDesc(PFPTokenPO::getRank);
			wrapperRank.eq(PFPTokenPO::getPickStatus, 1);
			Page<PFPTokenPO> rankListPage = pfpTokenMapper.selectPage(pageRank, wrapperRank);
			List<PFPTokenPO> rankList = rankListPage.getRecords();

			Page<PFPTokenPO> pageTime = new Page<>(current, 5);  // 指定查询第1页，每页5条
			LambdaQueryWrapper<PFPTokenPO> wrapperTime = new LambdaQueryWrapper<>();
			wrapperTime.orderByDesc(PFPTokenPO::getPriceTime);
			wrapperTime.eq(PFPTokenPO::getPickStatus, 1);
			List<PFPTokenPO> timeList = pfpTokenMapper.selectPage(pageTime, wrapperTime).getRecords();

			// 插入位置索引数组
			int[] insertIndexes = new int[]{3, 7, 11, 15, 19};
			for (int i = 0; i < timeList.size(); i++) {
				PFPTokenPO item = timeList.get(i);
				// 如果rankList已包含该元素，则跳过
				if (rankList.contains(item)) {
					continue;
				}

				// 如果insertIndexes[i]大于等于rankList的长度，则将元素添加到rankList末尾
				if (insertIndexes[i] >= rankList.size()) {
					rankList.addAll(timeList.subList(i, timeList.size()));
					break;
				} else {
					rankList.add(insertIndexes[i], item);
				}
			}

			Page<PFPTokenPageVo> result = new Page<>(current, size);
			BeanUtil.copyProperties(rankListPage, result);
			if (isLogin) {
				List<PFPTokenPageVo> pfpTokenPageVos = new ArrayList<>();
				if (result.getRecords() != null && result.getRecords().size() > 0) {
					pfpTokenPageVos = BeanUtil.copyProperties(rankList, PFPTokenPageVo.class);
					MemberPO memberPO = memberMapper.selectById(StpUtil.getLoginIdAsLong());
					for (PFPTokenPageVo x : pfpTokenPageVos) {
						x.setMatch(ScoreUtil.getMatch(memberPO.getUserTags(),
							memberPO.getCharisma(), memberPO.getExtroversion(), memberPO.getEnergy(),
							memberPO.getWisdom(), memberPO.getArt(), memberPO.getCourage(),
							x.getMintUserTags(),
							x.getCharisma(), x.getExtroversion(), x.getEnergy(),
							x.getWisdom(), x.getArt(), x.getCourage()));
					}
					List<PFPTokenPageVo> vos = setTokenPickCount(pfpTokenPageVos, rankList);
					result.setRecords(vos);

				}
			} else {
				List<PFPTokenPageVo> pfpTokenPageVos = new ArrayList<>();
				pfpTokenPageVos = BeanUtil.copyProperties(rankList, PFPTokenPageVo.class);
				List<PFPTokenPageVo> vos = setTokenPickCount(pfpTokenPageVos, rankList);
				result.setRecords(vos);
			}
			return R.data(result);
		}


		// -----------------------------------------------------------------


		wrapper.eq(PFPTokenPO::getMintStatus, 1);
		// 只展示未出价的
		if (pickStatus == 0) {
			wrapper.eq(PFPTokenPO::getPickStatus, 0);
		} else if (pickStatus == 1 && pageType != 1) {
			// 排除pickStatus为0的
			wrapper.ne(PFPTokenPO::getPickStatus, 0);
		}

		if (orderType == 0) {
			//降序
			if (orderColumn == 0) {
				// 按照match倒序
				wrapper.orderByDesc(PFPTokenPO::getLevelScore).orderByDesc(PFPTokenPO::getId);
			} else if (orderColumn == 1) {
//				wrapper.orderByDesc(PFPTokenPO::getM);
			} else if (orderColumn == 2) {
				wrapper.orderByDesc(PFPTokenPO::getPrice);
			} else if (orderColumn == 3) {
				wrapper.orderByDesc(PFPTokenPO::getLikes);
			} else if (orderColumn == 4) {
				// 在没有价格的情况下，根据mint NFT的顺序倒叙排列（即NFT的编号越大，排越前面），在有价格的情况，根据list的时间倒叙排列
				wrapper.orderByDesc(PFPTokenPO::getPriceTime).orderByDesc(PFPTokenPO::getId);
			}
		} else {
			//升序
			if (orderColumn == 0) {
				wrapper.orderByAsc(PFPTokenPO::getLevelScore).orderByAsc(PFPTokenPO::getId);
			} else if (orderColumn == 1) {
//				wrapper.orderByAsc(PFPTokenPO::getM);
			} else if (orderColumn == 2) {
				wrapper.orderByAsc(PFPTokenPO::getPrice);
			} else if (orderColumn == 3) {
				wrapper.orderByAsc(PFPTokenPO::getLikes);
			} else if (orderColumn == 4) {
				wrapper.orderByDesc(PFPTokenPO::getPriceTime);
			}
		}

		// 若pageType == 1 则查询跟当前用户存在member_connection表中的用户的NFT
		if (pageType != null && pageType == 1) {
			Long userId = StpUtil.getLoginIdAsLong();

			// userId == userId 以及 toUserId == userId 的记录
			List<MemberConnectPO> memberConnectionPOS = memberConnectMapper.selectList(new LambdaQueryWrapper<MemberConnectPO>()
				.eq(MemberConnectPO::getUserId, userId)
				.eq(MemberConnectPO::getIsDeleted, 0));
			List<Long> connectionIds = memberConnectionPOS.stream().map(MemberConnectPO::getToUserId).collect(Collectors.toList());
			List<MemberConnectPO> memberConnectionPOS2 = memberConnectMapper.selectList(new LambdaQueryWrapper<MemberConnectPO>()
				.eq(MemberConnectPO::getToUserId, userId)
				.eq(MemberConnectPO::getIsDeleted, 0));
			List<Long> connectionIds2 = memberConnectionPOS2.stream().map(MemberConnectPO::getUserId).collect(Collectors.toList());
			connectionIds.addAll(connectionIds2);
			if (connectionIds.size() > 0) {
				wrapper.in(PFPTokenPO::getOwnerUserId, connectionIds);
			} else if (connectionIds.size() == 0) {
				// 如果没有发布过nft and 没有关注过任何人，则返回空
				Page<PFPTokenPageVo> rest = new Page<>(current, size);
				return R.data(rest);
			}
		}

		page = pfpTokenMapper.selectPage(page, wrapper);

		Page<PFPTokenPageVo> result = new Page<>(current, size);
		BeanUtil.copyProperties(page, result);

		List<PFPTokenPageVo> pfpTokenPageVos = new ArrayList<>();
		if (page.getRecords() != null && page.getRecords().size() > 0) {
			pfpTokenPageVos = BeanUtil.copyProperties(page.getRecords(), PFPTokenPageVo.class);
		}

		MemberPO memberPO = new MemberPO();
		if (isLogin) {
			memberPO = memberMapper.selectById(StpUtil.getLoginIdAsLong());
		}

		for (PFPTokenPageVo x : pfpTokenPageVos) {
			if (isLogin) {
				x.setMatch(ScoreUtil.getMatch(memberPO.getUserTags(),
					memberPO.getCharisma(), memberPO.getExtroversion(), memberPO.getEnergy(),
					memberPO.getWisdom(), memberPO.getArt(), memberPO.getCourage(),
					x.getMintUserTags(),
					x.getCharisma(), x.getExtroversion(), x.getEnergy(),
					x.getWisdom(), x.getArt(), x.getCourage()));
			}
		}

		//match排序：
		if (orderColumn == 1) {
			if (orderType == 0) {
				//降序
				pfpTokenPageVos = pfpTokenPageVos.stream().sorted(Comparator.comparing(PFPTokenPageVo::getMatch).reversed()).collect(Collectors.toList());
			} else {
				//升序
				pfpTokenPageVos = pfpTokenPageVos.stream().sorted(Comparator.comparing(PFPTokenPageVo::getMatch)).collect(Collectors.toList());
			}
		}
		List<PFPTokenPageVo> vos = setTokenPickCount(pfpTokenPageVos, page.getRecords());
		result.setRecords(vos);
//		result.setRecords(pfpTokenPageVos);
		return R.data(result);
	}


	/**
	 * 获取token 本轮pick人数  pump当前nft得到的收益率
	 *
	 * @param list
	 * @return java.util.List<org.springblade.modules.admin.pojo.vo.PFPTokenPageVo>
	 * @author FengZi
	 * @date 14:39 2023/12/13
	 **/
	private List<PFPTokenPageVo> setTokenPickCount(List<PFPTokenPageVo> list, List<PFPTokenPO> tokenPOS) {
		List<Long> tokenIds = list.stream().map(s -> {
			return s.getRealTokenId();
		}).collect(Collectors.toList());
		if (list.size() == 0){
			return new ArrayList<>();
		}
		List<PFPPickPO> pickPOS = pfpPickMapper.selectList(new LambdaQueryWrapper<PFPPickPO>()
			.select(PFPPickPO::getTokenId, PFPPickPO::getNowPickCount, PFPPickPO::getCreateTime)
			.in(PFPPickPO::getTokenId, tokenIds));

		if (pickPOS != null && pickPOS.size() > 0) {
			//交易所合约
			Map<Long, String> marketAddressMap = tokenPOS.stream()
				.filter(pfpTokenPO -> pfpTokenPO.getContractMarketAddress() != null)
				.collect(
					Collectors.toMap(
						PFPTokenPO::getRealTokenId
						, PFPTokenPO::getContractMarketAddress
						, (existingValue, newValue) -> existingValue)
				);


			Map<Long, PFPPickPO> tokenIdToLatestPFPPickPOMap = pickPOS.stream()
				// 确保只处理 tokenId 和 time 都非 null 的记录
				.filter(pfpPickPO -> pfpPickPO.getTokenId() != null && pfpPickPO.getCreateTime() != null)
				.collect(Collectors.toMap(
					PFPPickPO::getTokenId, // 作为键的 tokenId
					Function.identity(), // 保持 PFPPickPO 对象不变
					(existing, replacement) -> existing.getCreateTime().compareTo(replacement.getCreateTime()) > 0 ? existing : replacement
					// 合并函数，选择时间更大（更新）的 PFPPickPO 对象
				));
			Map<Long, Integer> map = tokenIdToLatestPFPPickPOMap.entrySet().stream()
				// 过滤掉 nowPickCount 为 null 的记录
				.filter(entry -> entry.getValue().getNowPickCount() != null)
				.collect(Collectors.toMap(
					Map.Entry::getKey, // tokenId 为键
					entry -> entry.getValue().getNowPickCount() // nowPickCount 为值
				));
			for (PFPTokenPageVo x : list) {
				if (map.containsKey(x.getRealTokenId())) {
					x.setPickCount(map.get(x.getRealTokenId()));
				}
				if (marketAddressMap.containsKey(x.getRealTokenId())) {
					//判断新旧合约，如果是新合约,赋予当前pump能得到的收益率
					String newmarketAddress2 = contractProperties.getNewmarketAddress2();
					if (newmarketAddress2.equals(marketAddressMap.get(x.getRealTokenId()))) {
						Integer nowPickCount = x.getPickCount();
						switch (nowPickCount) {
							case 0:
								//语句
								x.setPumpRate(contractProperties.getPump1Rate().divide(BigInteger.valueOf(10)));
								break; //可选
							case 1:
								//语句
								x.setPumpRate(contractProperties.getPump2Rate().divide(BigInteger.valueOf(10)));
								break; //可选
							case 2:
								//语句
								x.setPumpRate(contractProperties.getPump3Rate().divide(BigInteger.valueOf(10)));
								break; //可选
							case 3:
								//语句
								x.setPumpRate(contractProperties.getPump4Rate().divide(BigInteger.valueOf(10)));
								break; //可选
							default: //可选
								x.setPumpRate(BigInteger.valueOf(4));
								break;
						}
					} else {
						x.setPumpRate(BigInteger.valueOf(4));
					}

				}
			}
		}
		return list;
	}

	// 设置单个nft的rank值
	@GetMapping("/setNFTRank")
	@ApiOperation(value = "设置单个nft的rank值")
	public R setNFTRank(@ApiParam(value = "Token ID(分页返回的id字段)", required = true) @RequestParam("id") Long id,
						@ApiParam(value = "rank值", required = false) @RequestParam(value = "rank", required = false) Long rank) {
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(id);
		if (pfpTokenPO == null) {
			return R.fail("nft not exist");
		}
		if (rank != null) {
			pfpTokenPO.setRank(rank);
			pfpTokenMapper.updateById(pfpTokenPO);
		}
		return R.data(pfpTokenPO.getRank());
	}

	// 获取单个nft的rank值
	@GetMapping("/getNFTRank")
	@ApiOperation(value = "获取单个nft的rank值")
	public R getNFTRank(@ApiParam(value = "Token ID(分页返回的id字段)", required = true) @RequestParam("id") Long id) {
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(id);
		if (pfpTokenPO == null) {
			return R.fail("nft not exist");
		}
		return R.data(pfpTokenPO.getRank());
	}

	@GetMapping("/getNFTDetail")
	@ApiOperation(value = "获取NFT详情")
	public R<PFPTokenDetailVo> getNFTDetail(@ApiParam(value = "Token ID(分页返回的id字段)", required = true) @RequestParam("id") Long id) {

//		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectById(id);
		PFPTokenPO pfpTokenPO = pfpTokenMapper.selectByRealTokenId(id);
		PFPTokenDetailVo result = new PFPTokenDetailVo();
		BeanUtil.copyProperties(pfpTokenPO, result);

		//判断合约地址是否为空，如果为空传递配置文件中合约地址。
		if (result.getContractMarketAddress() == null || result.getContractMarketAddress().length() == 0 || result.getPickStatus() == 0) {
			result.setContractMarketAddress(marketAddress);
		}

		// 设置链上id
		result.setRealTokenId(pfpTokenPO.getRealTokenId());

		//获取持有人信息 通过address查询
		MemberPO mintUser = memberMapper.selectById(pfpTokenPO.getMintUserId());
		MemberPO ownerUser = memberMapper.selectByAddress(pfpTokenPO.getOwnerAddress());

		// 是否存在该用户
		String ownerUserName = "";
		String ownerUserAvatar = "";
		if (ownerUser != null) {
			ownerUserName = ownerUser.getUserName();
			ownerUserAvatar = ownerUser.getAvatar();
		}

		result.setMintUserName(mintUser.getUserName());
		result.setMintUserAvatar(mintUser.getAvatar());
		result.setOwnerUserName(ownerUserName);
		result.setOwnerUserAvatar(ownerUserAvatar);

		//TODO 设置最高出价
		result.setBestPick(null);

		result.setIsMineMint(0);
		result.setIsMineOwner(0);

		//设置当前pick人数
		Long pickId = pfpTokenPO.getPickId();
		if (pickId != null) {
			PFPPickPO pfpPickPO = pfpPickMapper.selectById(pickId);
			result.setNowPickCount(pfpPickPO.getNowPickCount());

			//判断是否是新合约，如果是新合约就使用新的百分比
			if (marketAddress.equals(result.getContractMarketAddress())) {
				Integer nowPickCount = pfpPickPO.getNowPickCount();
				switch (nowPickCount) {
					case 0:
						//语句
						result.setNextPumpRate(contractProperties.getPump1Rate().divide(BigInteger.valueOf(10)));
						break; //可选
					case 1:
						//语句
						result.setNextPumpRate(contractProperties.getPump2Rate().divide(BigInteger.valueOf(10)));
						break; //可选
					default: //可选
						//语句
						result.setNextPumpRate(contractProperties.getPump3Rate().divide(BigInteger.valueOf(10)));
				}
			} else {
				result.setNextPumpRate(contractProperties.getPump3Rate().divide(BigInteger.valueOf(10)));
			}
		}
		if (marketAddress.equals(result.getContractMarketAddress())) {
			result.setContractVersion(contractProperties.getContractVersion());
		}

		boolean isLogin = StpUtil.isLogin();
		if (isLogin) {
			Long userId = StpUtil.getLoginIdAsLong();
			MemberPO memberPO = memberMapper.selectById(userId);

			result.setMatch(ScoreUtil.getMatch(memberPO.getUserTags(),
				memberPO.getCharisma(), memberPO.getExtroversion(), memberPO.getEnergy(),
				memberPO.getWisdom(), memberPO.getArt(), memberPO.getCourage(),
				result.getMintUserTags(),
				result.getCharisma(), result.getExtroversion(), result.getEnergy(),
				result.getWisdom(), result.getArt(), result.getCourage()));

			//是否为本人铸造
			if (pfpTokenPO.getMintUserId().equals(userId)) {
				result.setIsMineMint(1);
			}

			//是否为本人持有
			if (pfpTokenPO.getOwnerUserId().equals(userId)) {
				result.setIsMineOwner(1);
			}
		}

		//查询当前登录用户推荐人address
		if (isLogin) {
			Long loginIdAsLong = StpUtil.getLoginIdAsLong();
			MemberPO userByInverAddress = memberMapper.getUserByInviteAddress(loginIdAsLong);
			if (userByInverAddress != null) {
				result.setInviteAdress(userByInverAddress.getAddress());
			} else {
				//没有邀请人
				result.setInviteAdress("0x0000000000000000000000000000000000000000");
			}
		}


		return R.data(result);
	}


//	已移除该功能
//	@PostMapping("/collectNFTOnline")
//	@ApiOperation(value = "购买NFT(上链)")
//	public R<PFPTokenDetailVo> collectNFTOnline(@Valid @RequestBody CollectNFTQuery collectNFTQuery) throws Exception{
//
//		R result = nftService.collectNFTOnline(collectNFTQuery);
//		if(result.getCode() != 200){
//			return result;
//		}
//
//		return getNFTDetail(collectNFTQuery.getTokenId());
//	}

//	已移除该功能
//	@PostMapping("/collectCreateOrder")
//	@ApiOperation(value = "购买NFT创建订单")
//	public R<PFPTokenDetailVo> collectCreateOrder(@Valid @RequestBody CollectCreateOrderQuery collectCreateOrderQuery) throws Exception{
//
//		R result = nftService.collectCreateOrder(collectCreateOrderQuery);
//		if(result.getCode() != 200){
//			return result;
//		}
//
//		return getNFTDetail(collectCreateOrderQuery.getTokenId());
//	}

	@GetMapping("/getNFTHistory")
	@ApiOperation(value = "获取NFT的交易历史记录（History）")
	public R<List<PFPHistoryPO>> getNFTHistory(@ApiParam(value = "Token ID(分页返回的id字段)", required = true) @RequestParam("id") Long id) throws Exception {

		List<PFPHistoryPO> pfpHistoryPOS = pfpHistoryMapper.selectList(new LambdaQueryWrapper<PFPHistoryPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(PFPHistoryPO::getTokenId, id)
			.orderByDesc(BasePO::getCreateTime));

		Long now = System.currentTimeMillis();
		pfpHistoryPOS.forEach(x -> {
			Date createTime = x.getCreateTime();
			long diff = now - createTime.getTime();
			long second = diff / 1000;

			if (second < 60) {
				if (second == 1) {
					x.setTransTimeStr(second + " second ago");
				} else {
					x.setTransTimeStr(second + " seconds ago");
				}
			} else {
				long min = second / 60;
				if (min < 60) {
					if (min == 1) {
						x.setTransTimeStr(min + " min ago");
					} else {
						x.setTransTimeStr(min + " mins ago");
					}
				} else {
					long hour = min / 60;
					if (hour < 24) {
						if (hour == 1) {
							x.setTransTimeStr(hour + " hour ago");
						} else {
							x.setTransTimeStr(hour + " hours ago");
						}

					} else {
						long day = hour / 24;
						if (day == 1) {
							x.setTransTimeStr(day + " day ago");
						} else {
							x.setTransTimeStr(day + " days ago");
						}
					}
				}
			}
		});

		return R.data(pfpHistoryPOS);
	}
}
