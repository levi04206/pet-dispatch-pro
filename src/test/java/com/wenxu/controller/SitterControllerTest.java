package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.converter.SitterConverter;
import com.wenxu.dto.SitterApplyDTO;
import com.wenxu.entity.Sitter;
import com.wenxu.exception.GlobalExceptionHandler;
import com.wenxu.service.SitterService;
import com.wenxu.vo.SitterVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
        mockMvc = MockMvcBuilders.standaloneSetup(sitterController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void applySitterShouldUseCurrentUser() throws Exception {
        when(sitterService.applySitter(any(SitterApplyDTO.class), eq(100L))).thenReturn(true);

        mockMvc.perform(post("/api/sitter/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"张三\",\"phone\":\"13800138002\",\"idCard\":\"110101199001010011\",\"avatar\":\"https://example.com/avatar.jpg\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("入驻申请已提交，请等待管理员审核"));

        verify(sitterService).applySitter(any(SitterApplyDTO.class), eq(100L));
    }

    @Test
    void applySitterShouldReturnDuplicateErrorWhenServiceRejects() throws Exception {
        when(sitterService.applySitter(any(SitterApplyDTO.class), eq(100L))).thenReturn(false);

        mockMvc.perform(post("/api/sitter/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"张三\",\"phone\":\"13800138002\",\"idCard\":\"110101199001010011\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("您已提交过申请或已经是宠托师，请勿重复提交"));

        verify(sitterService).applySitter(any(SitterApplyDTO.class), eq(100L));
    }

    @Test
    void applySitterShouldRejectInvalidPhone() throws Exception {
        mockMvc.perform(post("/api/sitter/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"张三\",\"phone\":\"123\",\"idCard\":\"110101199001010011\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg", containsString("Phone format is invalid")));

        verify(sitterService, never()).applySitter(any(), eq(100L));
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
