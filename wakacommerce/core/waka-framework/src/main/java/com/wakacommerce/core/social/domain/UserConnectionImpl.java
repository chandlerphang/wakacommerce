
package com.wakacommerce.core.social.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 *
 * @ hui
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_UserConnection")
public class UserConnectionImpl implements UserConnection {

    @EmbeddedId
    UserConnectionPK userConnectionPK;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "displayName")
    private String displayName;

    @Column(name = "profileUrl")
    private String profileUrl;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Column(name = "accessToken", nullable = false)
    private String accessToken;

    @Column(name = "secret")
    private String secret;

    @Column(name = "refreshToken")
    private String refreshToken;

    @Column(name = "expireTime")
    private Long expireTime;

    @Override
    public UserConnectionPK getUserConnectionPK() {
        return userConnectionPK;
    }

    @Override
    public void setUserConnectionPK(UserConnectionPK userConnectionPK) {
        this.userConnectionPK = userConnectionPK;
    }

    @Override
    public Integer getRank() {
        return rank;
    }

    @Override
    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getProfileUrl() {
        return profileUrl;
    }

    @Override
    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public Long getExpireTime() {
        return expireTime;
    }

    @Override
    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public static class UserConnectionPK implements Serializable {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Column(name = "userId", nullable = false)
        private String userId;

        @Column(name = "providerId", nullable = false)
        private String providerId;

        @Column(name = "providerUserId")
        private String providerUserId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public String getProviderUserId() {
            return providerUserId;
        }

        public void setProviderUserId(String providerUserId) {
            this.providerUserId = providerUserId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            else if (!getClass().isAssignableFrom(obj.getClass())) return false;

            return userId.equals(((UserConnectionPK) obj).getUserId()) &&
                    providerId.equals(((UserConnectionPK) obj).getProviderId()) &&
                    providerUserId.equals(((UserConnectionPK) obj).getProviderUserId());
        }

        @Override
        public int hashCode() {
            return userId.hashCode() + providerId.hashCode() + providerUserId.hashCode();
        }
    }

}
