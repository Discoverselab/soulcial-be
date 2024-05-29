package org.springblade.modules.admin.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.dao.PFPTokenMapper;
import org.springblade.modules.admin.dao.WhiteListMapper;
import org.springblade.modules.admin.pojo.enums.NFTColorEnum;
import org.springblade.modules.admin.pojo.enums.NFTLevelEnum;
import org.springblade.modules.admin.pojo.enums.NFTMoodEnum;
import org.springblade.modules.admin.pojo.enums.NFTPersonalityEnum;
import org.springblade.modules.admin.pojo.po.BasePO;
import org.springblade.modules.admin.pojo.po.PFPTokenPO;
import org.springblade.modules.admin.pojo.po.WhiteListPO;
import org.springblade.modules.admin.util.AddressUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/whiteList")
@Api(value = "白名单",tags = "白名单")
public class WhiteListController {

	@Autowired
	WhiteListMapper whiteListMapper;

	@PostMapping("/importWhiteList")
	@ApiOperation(value = "导入白名单")
	public R importWhiteList(@RequestParam MultipartFile file) throws Exception{
		ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
		Integer count = 0;
		List<List<Object>> read = reader.read();
		for (int i=0; i< read.size(); i++) {
			//获取每行第一个
			Object obj = read.get(i).get(0);
			if(obj != null){
				String address = obj.toString();
				//校验是否是ETH地址
				boolean ethAddress = AddressUtil.isETHAddress(address);
				if(!ethAddress){
					return R.fail("import fail! Address: " + address + " is not a legal address at line " + (i+1));
				}

				if(StringUtils.isNotBlank(address)){

					WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
						.eq(BasePO::getIsDeleted, 0)
						.eq(WhiteListPO::getAddress, address));

					if(whiteListPO == null){
						whiteListPO = new WhiteListPO();
						whiteListPO.setAddress(address);
						whiteListPO.initForInsertNoAuth();

						whiteListMapper.insert(whiteListPO);
						count++;
					}
				}
			}
		}
		return R.success("import white list success:"+count);
	}

	@PostMapping("/importWhiteList2")
	@ApiOperation(value = "导入白名单（可以mint两次）")
	public R importWhiteList2(@RequestParam MultipartFile file) throws Exception{
		ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
		Integer count = 0;
		List<List<Object>> read = reader.read();
		for (int i=0; i< read.size(); i++) {
			//获取每行第一个
			Object obj = read.get(i).get(0);
			if(obj != null){
				String address = obj.toString();
				//校验是否是ETH地址
				boolean ethAddress = AddressUtil.isETHAddress(address);
				if(!ethAddress){
					return R.fail("import fail! Address: " + address + " is not a legal address at line " + (i+1));
				}

				if(StringUtils.isNotBlank(address)){

					WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
						.eq(BasePO::getIsDeleted, 0)
						.eq(WhiteListPO::getAddress, address));

					if(whiteListPO == null){
						whiteListPO = new WhiteListPO();
						whiteListPO.setAddress(address);
						whiteListPO.initForInsertNoAuth();
						whiteListPO.setCanMintTwice(1);

						whiteListMapper.insert(whiteListPO);
						count++;
					}
				}
			}
		}
		return R.success("import white list success:"+count);
	}

	@PostMapping("/deleteWhiteList")
	@ApiOperation(value = "白名单移除")
	public R deleteWhiteList(@RequestParam("address") String address){

		WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(WhiteListPO::getAddress, address));

		if(whiteListPO != null) {
			whiteListPO.initForUpdateNoAuth();
			whiteListPO.setIsDeleted(1);

			whiteListMapper.updateById(whiteListPO);
		}else {
			return R.fail("address is not exist in white list");
		}
		return R.success("remove success");
	}

	@PostMapping("/addWhiteList")
	@ApiOperation(value = "白名单添加：单个地址")
	public R addWhiteList(@RequestParam("address") String address){

		//校验是否是ETH地址
		boolean ethAddress = AddressUtil.isETHAddress(address);
		if(!ethAddress){
			return R.fail("add fail! Address: " + address + " is not a legal address");
		}

		WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(WhiteListPO::getAddress, address));

		if(whiteListPO == null) {
			whiteListPO = new WhiteListPO();
			whiteListPO.setAddress(address);
			whiteListPO.initForInsertNoAuth();

			whiteListMapper.insert(whiteListPO);
		}
		return R.success("add white list success");
	}

	@GetMapping("/checkWhiteList")
	@ApiOperation(value = "检验是否在白名单中")
	public R<Boolean> checkWhiteList(@RequestParam("address") String address){

		WhiteListPO whiteListPO = whiteListMapper.selectOne(new LambdaQueryWrapper<WhiteListPO>()
			.eq(BasePO::getIsDeleted, 0)
			.eq(WhiteListPO::getAddress, address));

		if(whiteListPO != null) {
			return R.data(true,"in white list");
		}else {
			return R.data(false,"not in white list");
		}
	}

}
