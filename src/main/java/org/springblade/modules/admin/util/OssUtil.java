package org.springblade.modules.admin.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import org.springblade.core.oss.props.OssProperties;
import org.springblade.core.tool.api.R;
import org.springblade.modules.admin.cache.IUserCache;
import org.springblade.modules.admin.dao.ChatOverviewMapper;
import org.springblade.modules.admin.dao.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

@Component
public class OssUtil {


	private static OssProperties ossProperties;

	@Autowired
	public void setOssProperties(OssProperties ossProperties) {
		OssUtil.ossProperties = ossProperties;
	}

	private static OSS ossClient;

	@Autowired
	public void setOssClient(OSS ossClient) {
		OssUtil.ossClient = ossClient;
	}

	public static String getTwitterAvatar(String url) {
		// 时间戳文件名 + 原始文件名后缀
		String fileName = System.currentTimeMillis() + extractFileName(url);
		String bucketName = ossProperties.getBucketName();
		try {
			InputStream in = new BufferedInputStream(new URL(url).openStream());
			PutObjectResult result = ossClient.putObject(bucketName, fileName, in);
			return "https://" + bucketName + "." + ossProperties.getEndpoint() + "/" + fileName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getUrlByFile(MultipartFile file) {
		String fileName = System.currentTimeMillis() + file.getOriginalFilename();
		String bucketName = ossProperties.getBucketName();
		try {
			InputStream inputStream = file.getInputStream();
			PutObjectResult result = ossClient.putObject(bucketName, fileName, inputStream);
//			String uri = result.getResponse().getUri();
			return "https://" + bucketName + "." + ossProperties.getEndpoint() + "/" + fileName;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static String extractFileName(String url) {
		// 检查URL是否为空或空格
		if (url == null || url.trim().isEmpty()) {
			return "";
		}

		// 找到最后一个斜线后的内容
		int lastSlashIndex = url.lastIndexOf('/');

		// 截取URL最后一个斜线后面的部分作为文件名
		return lastSlashIndex >= 0 ? url.substring(lastSlashIndex + 1) : url;
	}


}
