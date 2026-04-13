package com.wenxu.service;

import com.wenxu.entity.User;

public interface UserService {

    boolean sendCode(String phone);

    String login(String phone, String code);

    User testInsertAndQuery();
}
