package com.wenxu.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenxu.entity.OperationLog;
import com.wenxu.service.OperationLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminOperationLogControllerTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private AdminOperationLogController adminOperationLogController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminOperationLogController).build();
    }

    @Test
    void pageLogsShouldReturnPagedLogs() throws Exception {
        OperationLog operationLog = buildLog();
        Page<OperationLog> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setPages(1);
        page.setRecords(List.of(operationLog));

        when(operationLogService.pageLogs(1L, 10L, 1001L, "ADMIN", "订单", "取消", "FAILED",
                LocalDateTime.of(2026, 4, 21, 10, 0),
                LocalDateTime.of(2026, 4, 21, 20, 0)))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/operation-log/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("userId", "1001")
                        .param("role", "ADMIN")
                        .param("module", "订单")
                        .param("keyword", "取消")
                        .param("resultType", "FAILED")
                        .param("startTime", "2026-04-21T10:00")
                        .param("endTime", "2026-04-21T20:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].resultText").value("失败"))
                .andExpect(jsonPath("$.data.records[0].success").value(false));

        ArgumentCaptor<Long> pageNumCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> pageSizeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> moduleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> resultTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(operationLogService).pageLogs(pageNumCaptor.capture(), pageSizeCaptor.capture(), userIdCaptor.capture(),
                roleCaptor.capture(), moduleCaptor.capture(), keywordCaptor.capture(), resultTypeCaptor.capture(),
                startCaptor.capture(), endCaptor.capture());
        assertEquals(1L, pageNumCaptor.getValue());
        assertEquals(10L, pageSizeCaptor.getValue());
        assertEquals(1001L, userIdCaptor.getValue());
        assertEquals("ADMIN", roleCaptor.getValue());
        assertEquals("订单", moduleCaptor.getValue());
        assertEquals("取消", keywordCaptor.getValue());
        assertEquals("FAILED", resultTypeCaptor.getValue());
        assertEquals(LocalDateTime.of(2026, 4, 21, 10, 0), startCaptor.getValue());
        assertEquals(LocalDateTime.of(2026, 4, 21, 20, 0), endCaptor.getValue());
    }

    @Test
    void pageLogsShouldRejectInvalidTimeRange() throws Exception {
        mockMvc.perform(get("/api/admin/operation-log/page")
                        .param("startTime", "2026-04-21T20:00")
                        .param("endTime", "2026-04-21T10:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("开始时间不能晚于结束时间"));

        verify(operationLogService, never()).pageLogs(1L, 10L, null, null, null, null, null, null, null);
    }

    @Test
    void getDetailShouldReturnOperationLogDetail() throws Exception {
        when(operationLogService.getById(1L)).thenReturn(buildLog());

        mockMvc.perform(get("/api/admin/operation-log/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.resultText").value("失败"))
                .andExpect(jsonPath("$.data.requestPath").value("POST /api/orders/cancel"));
    }

    @Test
    void getDetailShouldReturnErrorWhenNotFound() throws Exception {
        when(operationLogService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/admin/operation-log/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("操作日志不存在"));
    }

    private OperationLog buildLog() {
        OperationLog operationLog = new OperationLog();
        operationLog.setId(1L);
        operationLog.setUserId(1001L);
        operationLog.setRole("ADMIN");
        operationLog.setModule("订单模块");
        operationLog.setAction("取消订单 [FAILED] 订单状态不允许");
        operationLog.setRequestPath("POST /api/orders/cancel");
        operationLog.setIp("127.0.0.1");
        operationLog.setCostTimeMs(18L);
        operationLog.setCreateTime(LocalDateTime.of(2026, 4, 21, 20, 0, 0));
        return operationLog;
    }
}
