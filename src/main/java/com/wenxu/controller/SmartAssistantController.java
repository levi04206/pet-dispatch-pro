package com.wenxu.controller;

import com.wenxu.common.Result;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/assistant")
public class SmartAssistantController {

    private static final String SYSTEM_PROMPT = """
            你是 pet-dispatch-pro 宠物上门服务调度平台的智能派单助手。
            你可以帮助用户查询订单状态、解释订单进度、推荐合适的宠托师，并给出派单建议。
            当用户问题涉及订单详情或宠托师推荐时，必须优先调用系统提供的工具函数获取真实数据。
            你不能编造订单、宠托师、金额、评分、时间等业务数据。
            如果工具返回未找到数据，要如实说明未查询到或当前用户无权访问。
            回答使用中文，语气专业、简洁、可执行。
            """;

    private final ChatClient chatClient;

    public SmartAssistantController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String message) {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withFunctions(Set.of("queryOrderDetail", "recommendSitters"))
                .build();

        Prompt prompt = new Prompt(
                List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(message)),
                chatOptions
        );
        ChatResponse response = chatClient.call(prompt);
        return Result.success(response.getResult().getOutput().getContent());
    }
}
