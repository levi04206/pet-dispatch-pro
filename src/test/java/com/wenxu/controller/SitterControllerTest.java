package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.converter.SitterConverter;
import com.wenxu.entity.Sitter;
import com.wenxu.service.SitterService;
import com.wenxu.vo.SitterVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SitterControllerTest {

    @Mock
    private SitterService sitterService;

    @Mock
    private SitterConverter sitterConverter;

    @InjectMocks
    private SitterController sitterController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(100L);
        mockMvc = MockMvcBuilders.standaloneSetup(sitterController).build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void getMyProfileShouldReturnSitterVO() throws Exception {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        SitterVO vo = new SitterVO();
        vo.setId(10L);
        vo.setRealName("张三");

        when(sitterService.getMyProfile(100L)).thenReturn(sitter);
        when(sitterConverter.toVO(sitter)).thenReturn(vo);

        mockMvc.perform(get("/api/sitter/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.realName").value("张三"));
    }

    @Test
    void switchWorkStatusShouldUseCurrentUser() throws Exception {
        when(sitterService.switchWorkStatus(100L, 1)).thenReturn(true);

        mockMvc.perform(post("/api/sitter/workStatus").param("workStatus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("已切换为接单中"));

        verify(sitterService).switchWorkStatus(100L, 1);
    }
}
