package com.wenxu.service;

public interface UserService {

    boolean sendCode(String phone);

    String login(String phone, String code);
}
