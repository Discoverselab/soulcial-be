package org.springblade.modules.admin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.pojo.vo.CutPictureVo;
import org.springblade.modules.admin.pojo.vo.MintPictureVo;
import org.springblade.modules.admin.util.AWSUpload;
import org.springblade.modules.admin.util.ColorTools;
import org.springblade.modules.admin.util.OssUtil;
import org.springblade.modules.system.entity.Dict;
import org.springblade.modules.system.mapper.DictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

@RestController
@RequestMapping("/api/admin/AIGC")
@Api(value = "AIGC开关",tags = "AIGC开关")
public class AigcController {

	private final static String ARSEEDING_UPLOAD_URL = "https://arseed.web3infra.dev/bundle/data/USDC";

	private final static String ARSEEDING_PIC_URL = "https://arseed.web3infra.dev/";

	@Autowired
	DictMapper dictMapper;

	@Autowired
	AWSUpload awsUpload;

	@PostMapping("/closeAIGC")
	@ApiOperation(value = "关闭AIGC")
	public R closeAIGC() {

		Dict aicg_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));

		//0-关闭
		aicg_switch.setDictValue("0");

		dictMapper.updateById(aicg_switch);
		return R.success("close success");
	}

	@PostMapping("/openAIGC")
	@ApiOperation(value = "打开AIGC")
	public R openAIGC() {

		Dict aicg_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));

		//1-打开
		aicg_switch.setDictValue("1");

		dictMapper.updateById(aicg_switch);
		return R.success("open success");
	}

	// 打开AIGC定时任务
	@PostMapping("/openAIGCJob")
	@ApiOperation(value = "打开AIGC定时任务")
	public R openAIGCJob() {
		Dict aicg_scheduled_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_scheduled_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));
		// 1-打开
		aicg_scheduled_switch.setDictValue("1");
		dictMapper.updateById(aicg_scheduled_switch);
		return R.success("open success");
	}

	// 关闭AIGC定时任务
	@PostMapping("/closeAIGCJob")
	@ApiOperation(value = "关闭AIGC定时任务")
	public R closeAIGCJob() {
		Dict aicg_scheduled_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_scheduled_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));
		// 0-关闭
		aicg_scheduled_switch.setDictValue("0");
		dictMapper.updateById(aicg_scheduled_switch);
		return R.success("close success");
	}

	// 获取AIGC定时任务状态
	@PostMapping("/getAIGCJobStatus")
	@ApiOperation(value = "获取AIGC定时任务状态")
	public R getAIGCJobStatus() {
		Dict aicg_scheduled_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_scheduled_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));
		if(aicg_scheduled_switch.getDictValue().equals("0")){
			return R.data("关闭");
		} else {
			return R.data("打开");
		}
	}



	@PostMapping("/getAIGCStatus")
	@ApiOperation(value = "获取AIGC开关状态")
	public R getAIGCStatus() {

		Dict aicg_switch = dictMapper.selectOne(new LambdaQueryWrapper<Dict>()
			.eq(Dict::getCode, "aicg_switch")
			.eq(Dict::getDictKey, "0")
			.eq(Dict::getIsDeleted, 0));

		if(aicg_switch.getDictValue().equals("0")){
			return R.data("关闭");
		}else {
			return R.data("打开");
		}
	}

	@ApiOperation("图片裁剪成6变形 上传到4everland P0")
	@PostMapping("cutAndUpload")
	public R<MintPictureVo> cutAndUpload(@RequestBody CutPictureVo cutPictureVo){
		MintPictureVo mintPictureVo = new MintPictureVo();
		try {
			//AIGC生成的方形图片
			BufferedImage read = ImageIO.read(new URL(cutPictureVo.getSquarePictureUrl()));
			Color color = new Color(read.getRGB(1, 1));
			//图片颜色属性
			int colorAttribute = ColorTools.getH(color);

			//上传ARSEEDING
//			String requestUrl = ARSEEDING_UPLOAD_URL + "?Content-Type=image/png";
//			String response = HttpRequest.post(requestUrl)
//				.header("X-API-KEY","19a1f1db-0f1b-11ee-b53e-1eb4fcb58e6f")
//				.header("Content-Type","image/jpeg")
//				.body(decode)
//				.timeout(HttpGlobalConfig.getTimeout()).execute().body();
//
//			JSONObject obj = JSONObject.parseObject(response);
//			String itemId = obj.getString("itemId");
			//不规则URL
//			String pictureUrl = ARSEEDING_PIC_URL + itemId;
			//方形图片上传ARSEEDING
//			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//			boolean png = ImageIO.write(read, "png", byteArrayOutputStream);
//
//			String squareResponse = HttpRequest.post(requestUrl)
//				.header("X-API-KEY","19a1f1db-0f1b-11ee-b53e-1eb4fcb58e6f")
//				.header("Content-Type","image/jpeg")
//				.body(byteArrayOutputStream.toByteArray())
//				.timeout(HttpGlobalConfig.getTimeout()).execute().body();
//
//			JSONObject squareObj = JSONObject.parseObject(squareResponse);
//			String squareItemId = squareObj.getString("itemId");

			//不规则URL
//			String squarePictureUrl = ARSEEDING_PIC_URL + squareItemId;
			// 上传规则图片到4everland
			// 随机fileName 哈希形式，后缀.png
			String randomStr = String.valueOf(System.currentTimeMillis());
			String fileName = randomStr + ".png";
			// BufferedImage read转换为File
			File outputFile = new File(fileName);
			ImageIO.write(read, "png", outputFile);
			String squarePictureUrl = awsUpload.uploadFileByFile(outputFile, fileName);

			// 上传裁剪后的到4everland
			//图片裁剪
//
			String body = "";
			try {
//				body = HttpUtil.get("api.soulcial.network:2008/?img_url=" + cutPictureVo.getSquarePictureUrl());
				body = HttpUtil.get("localhost:2008/?img_url=" + cutPictureVo.getSquarePictureUrl());
			} catch (Exception e) {
				e.printStackTrace();
				return R.fail("something wrong.please try again, cut error");
			}
			byte[] decode = Base64.getDecoder().decode(body);
			// 判断decode是否合法
			if(decode.length == 0){
				return R.fail("something wrong.please try again, cut error");
			}
			String cutFileName = randomStr + "_cut.png";
			File cutOutputFile = new File(cutFileName);
			System.out.println("decode length----> " + Arrays.toString(decode));
			ImageIO.write(ImageIO.read(new ByteArrayInputStream(decode)), "png", cutOutputFile);
			String pictureUrl = awsUpload.uploadFileByFile(cutOutputFile, cutFileName);

			// 清除临时file
			FileUtil.del(outputFile);
			FileUtil.del(cutOutputFile);

			mintPictureVo.setPictureUrl(pictureUrl);
			mintPictureVo.setSquarePictureUrl(squarePictureUrl);
			mintPictureVo.setColorAttribute(colorAttribute);
		}catch (Exception e){
			e.printStackTrace();
			return R.fail("someting wrong.please try again");
		}
		return  R.data(mintPictureVo);
	}

//	@ApiOperation("图片上传到arseeding")
//	@PostMapping("pictureUpload")
//	public R<String> pictureUpload(File file){
//		try {
//			byte[] decode = file.getBytes();
//			//上传ARSEEDING
//			String requestUrl = ARSEEDING_UPLOAD_URL + "?Content-Type=image/png";
//			String response = HttpRequest.post(requestUrl)
//				.header("X-API-KEY","19a1f1db-0f1b-11ee-b53e-1eb4fcb58e6f")
//				.header("Content-Type","image/jpeg")
//				.body(decode)
//				.timeout(HttpGlobalConfig.getTimeout()).execute().body();
//
//			JSONObject obj = JSONObject.parseObject(response);
//			String itemId = obj.getString("itemId");
//
//			//图片URL
//			String pictureUrl = ARSEEDING_PIC_URL + itemId;
//			return  R.data(pictureUrl);
//		}catch (Exception e){
//			e.printStackTrace();
//			return R.fail("someting wrong.please try again");
//		}
//
//	}
	@ApiOperation("图片上传到4everland")
	@PostMapping("pictureUpload")
	public R<String> pictureUpload(MultipartFile file){
		try {
//			// 通过文件上传到4everland，随机文件名
//			String randomStr = String.valueOf(System.currentTimeMillis());
//			// 添加提交的file的后缀名
//			String fileName = randomStr + ".png";
//			String pictureUrl = awsUpload.uploadFileByMultiPartFile(file, fileName);
			String pictureUrl = OssUtil.getUrlByFile(file);
			if ("".equals(pictureUrl)) {
				return R.fail("something wrong.please try again");
			}
			return  R.data(pictureUrl);
		}catch (Exception e){
			e.printStackTrace();
			return R.fail("someting wrong.please try again");
		}
	}

}
