/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.common.utils;

import cn.hutool.http.HttpUtil;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 通用工具类
 *
 * @author Chill
 */
@Slf4j
public class CommonUtil {

	public static String uploadFile(S3Client s3Client, String bucketName, String keyId, String accessKeyId, String accessToken, byte[] data) {
//		PutObjectRequest putRequest = PutObjectRequest.builder()
//			.bucket(bucketName)
//			.key(apiKey)
//			.build()
//		// 创建PutObject请求
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucketName)
			.key(keyId)
			.build();

		// 使用 String keyId, String accessKeyId, String accessToken 发送
		// 创建RequestBody对象
		RequestBody requestBody = RequestBody.fromBytes(data);

		// 设置s3Client区域及上传对象
		PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);

		return putObjectResponse.eTag();


//		PutObjectResponse response;
//		try {
//			response = s3Client.putObject(putRequest, requestBody).get();
//		} catch (Exception e) {
//			log.error("Failed to upload file to S3", e);
//			throw new RuntimeException("Failed to upload file to S3", e);
//		}
//
//		return response.eTag();
	}

	// 获取getSessionToken
	public static Credentials getCredentials(String accessKeyId, String secretAccessKey) throws URISyntaxException {
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create("0xd3adb6c64cddc9f08566b8aba1129904d5b1b100", "sApu0iOUhLYVyVedH1j6ZRg8CAMqSEgXxzXxYUbr");

		StsClient sts = StsClient.builder()
			.endpointOverride(new URI("https://endpoint.4everland.co"))
			.region(Region.AP_EAST_1)
			.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
			.build();

		AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
			.roleArn("arn:aws:iam::account-id:role/soulcial") // 替换为你的IAM角色ARN
			.roleSessionName("only-put-object")
			.durationSeconds(3600)
			.build();

		AssumeRoleResponse roleResponse = sts.assumeRole(roleRequest);

		return roleResponse.credentials();
	}
}
