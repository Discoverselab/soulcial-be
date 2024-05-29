package org.springblade.modules.admin.controller;

import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springblade.core.tool.api.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.security.Security;

@RestController
@Slf4j
@RequestMapping("/api/notification")
@Api(value = "消息通知", tags = "消息通知")
public class NotificationController {

	@Value("${webPush.publicKey}")
	private String publicKey;

	@Value("${webPush.privateKey}")
	private String privateKey;

	@Value("${webPush.email}")
	private String email;

	private Subscription subscription;

	@PostMapping("/subscribe")
	public void subscribe(@RequestBody Subscription subscription) {
		this.subscription = subscription;
		log.info("Subscription registered: {}", subscription);
	}

	@Getter
	public static class NotificationRequest {
		private String title;
		private String body;
		private String icon;
		private String url;
	}

	@Getter
	public static class Subscription {
		private String endpoint;
		private Keys keys;

		@Getter
		public static class Keys {
			private String p256dh;
			private String auth;

		}
	}

	@GetMapping("/getSubscription")
	public R getSubscription() {
		return R.data(subscription);
	}

	@PostMapping("/trigger")
	public R triggerNotification(@RequestBody NotificationRequest request) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		PushService pushService = new PushService(
			publicKey,
			privateKey,
			email
		);

		String jsonPayload = "{\"title\":\"" + request.getTitle() + "\",\"body\":\"" + request.getBody() + "\",\"icon\":\"" + request.getIcon() + "\",\"url\":\"" + request.getUrl() + "\"}";
		byte[] payload = jsonPayload.getBytes();

//		byte[] payload = message.getBytes();

		Notification notification = new Notification(
			subscription.endpoint,
			subscription.keys.p256dh,
			subscription.keys.auth,
			payload
		);

		HttpResponse httpResponse = pushService.send(notification);

		if(httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
			System.out.println("Failed to send notification: " + httpResponse.getStatusLine());
			throw new RuntimeException("Failed to send notification");
		}
		return R.success("success " + httpResponse.getStatusLine());
	}
}
