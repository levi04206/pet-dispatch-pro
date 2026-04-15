package com.wenxu.controller;

import com.wenxu.common.ApiMessages;
import com.wenxu.common.Result;
import com.wenxu.common.ResultCodeEnum;
import com.wenxu.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/sendCode")
    public Result<String> sendCode(@RequestParam String phone) {
        boolean sent = userService.sendCode(phone);
        if (!sent) {
            return Result.error(ResultCodeEnum.PARAM_ERROR);
        }
        return Result.success(ApiMessages.CODE_SENT);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestParam String phone, @RequestParam String code) {
        String token = userService.login(phone, code);
        if (token == null) {
            return Result.error(ApiMessages.CODE_INVALID);
        }
        return Result.success(token);
    }
}
