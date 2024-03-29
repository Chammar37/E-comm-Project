package com.mnp.store.contracts.users.dtos;

import com.mnp.store.domain.users.User;

public class UserResponseDto {
    private String username;
    private String email;

    public UserResponseDto() {
    }

    public UserResponseDto(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public UserResponseDto(User user) {
        this(user.getUsername(), user.getEmail());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
