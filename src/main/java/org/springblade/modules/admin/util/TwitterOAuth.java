package org.springblade.modules.admin.util;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;

public class TwitterOAuth {

	public TwitterOAuth() {
	}

	private OAuth10aService service;
	private OAuth1RequestToken requestToken;

	private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/2/users/me?user.fields=public_metrics,id,name,username,profile_image_url";

//	private static final String API_KEY = "QZ7qhmXXmy5p8Px9hDcw9Q26W";
	private static final String API_KEY = "o1JHqZAxqIq7UWy7fDoWrpPhY";

//	private static final String API_SECRET = "G5yod67UJdjkX12a4j8djPXdNRmDtadQ4ECZLBigKdlaK9DGRK";
	private static final String API_SECRET = "57FtMTV7LOiYxq6unRdOC2YDVWwfBSDaQ9uGZw3DY42y3DdL0y";

	public String getRedirectUrl(String callbackUrl) throws IOException, InterruptedException, ExecutionException {
		service = new ServiceBuilder(API_KEY)
			.apiSecret(API_SECRET)
			.callback(callbackUrl)
			.build(TwitterApi.instance());
		requestToken = service.getRequestToken();
		return service.getAuthorizationUrl(requestToken);
	}

	public String setTwitterUserInfo(String oauthVerifier) throws IOException, ExecutionException, InterruptedException {
		// 判断service是否初始化，若未初始化，则初始化
		if (service == null) {
			service = new ServiceBuilder(API_KEY)
				.apiSecret(API_SECRET)
				.build(TwitterApi.instance());
			requestToken = service.getRequestToken();
		}
		final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oauthVerifier);
		System.out.println("Got the Access Token!");
		System.out.println("(The raw response looks like this: " + accessToken.getRawResponse() + "')");
		System.out.println();

		// Now let's go and ask for a protected resource!
		System.out.println("Now we're going to access a protected resource...");
		final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
		service.signRequest(accessToken, request);
		try (Response response = service.execute(request)) {
			System.out.println("Got it! Lets see what we found...");
			System.out.println();
			System.out.println(response.getBody());
			return response.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		}
	}

	public String main(String callbackUrl) throws IOException, InterruptedException, ExecutionException {
		return getRedirectUrl(callbackUrl);
	}
}
