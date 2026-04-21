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
        OperationLog operationLog = new OperationLog();
        operationLog.setId(1L);
        operationLog.setUserId(1001L);
        operationLog.setRole("ADMIN");
        operationLog.setModule("订单模块");
        operationLog.setAction("取消订单");
        operationLog.setRequestPath("POST /api/orders/cancel");
        operationLog.setIp("127.0.0.1");
        operationLog.setCostTimeMs(18L);
        operationLog.setCreateTime(LocalDateTime.of(2026, 4, 21, 20, 0, 0));

        Page<OperationLog> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setPages(1);
        page.setRecords(List.of(operationLog));

        when(operationLogService.pageLogs(1L, 10L, 1001L, "ADMIN", "订单", "取消"))
                .thenReturn(page);

        mockMvc.perform(get("/api/admin/operation-log/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("userId", "1001")
                        .param("role", "ADMIN")
                        .param("module", "订单")
                        .param("keyword", "取消"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].module").value("订单模块"))
                .andExpect(jsonPath("$.data.records[0].action").value("取消订单"));

        ArgumentCaptor<Long> pageNumCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> pageSizeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> moduleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        verify(operationLogService).pageLogs(pageNumCaptor.capture(), pageSizeCaptor.capture(), userIdCaptor.capture(),
                roleCaptor.capture(), moduleCaptor.capture(), keywordCaptor.capture());
        assertEquals(1L, pageNumCaptor.getValue());
        assertEquals(10L, pageSizeCaptor.getValue());
        assertEquals(1001L, userIdCaptor.getValue());
        assertEquals("ADMIN", roleCaptor.getValue());
        assertEquals("订单", moduleCaptor.getValue());
        assertEquals("取消", keywordCaptor.getValue());
    }
}
