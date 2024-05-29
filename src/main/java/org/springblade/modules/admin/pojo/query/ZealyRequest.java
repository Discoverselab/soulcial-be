package org.springblade.modules.admin.pojo.query;

import lombok.Data;

@Data
public class ZealyRequest {
    private String userId;
    private String communityId;
    private String subdomain;
    private String questId;
    private String requestId;
    private Accounts accounts;

    // Getters and setters for all fields...

	@Data
    public static class Accounts {
        private String email;
        private String wallet;
        private Discord discord;
        private Twitter twitter;

        // Getters and setters for all fields...

		@Data
        public static class Discord {
            private String id;
            private String handle;

            // Getters and setters for all fields...
        }

		@Data
        public static class Twitter {
            private String id;
            private String username;

            // Getters and setters for all fields...
        }
    }
}
