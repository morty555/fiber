package com.example.srp.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    private String adminSecretKey;
    private String userSecretKey;
    private long adminTtl;
    private long userTtl;
    private String adminTokenName;
    private String userTokenName;

    // Getter & Setter

    public String getAdminSecretKey() {
        return adminSecretKey;
    }

    public void setAdminSecretKey(String adminSecretKey) {
        this.adminSecretKey = adminSecretKey;
    }

    public String getUserSecretKey() {
        return userSecretKey;
    }

    public void setUserSecretKey(String userSecretKey) {
        this.userSecretKey = userSecretKey;
    }

    public long getAdminTtl() {
        return adminTtl;
    }

    public void setAdminTtl(long adminTtl) {
        this.adminTtl = adminTtl;
    }

    public long getUserTtl() {
        return userTtl;
    }

    public void setUserTtl(long userTtl) {
        this.userTtl = userTtl;
    }

    public String getAdminTokenName() {
        return adminTokenName;
    }

    public void setAdminTokenName(String adminTokenName) {
        this.adminTokenName = adminTokenName;
    }

    public String getUserTokenName() {
        return userTokenName;
    }

    public void setUserTokenName(String userTokenName) {
        this.userTokenName = userTokenName;
    }
}

