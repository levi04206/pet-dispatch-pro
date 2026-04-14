package com.wenxu.controller.admin;

import com.wenxu.converter.SitterConverter;
import com.wenxu.entity.Sitter;
import com.wenxu.service.SitterService;
import com.wenxu.vo.SitterVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSitterControllerTest {

    @Mock
    private SitterService sitterService;

    @Mock
    private SitterConverter sitterConverter;

    @InjectMocks
    private AdminSitterController adminSitterController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminSitterController).build();
    }

    @Test
    void listPendingApplicationsShouldReturnSitterVOList() throws Exception {
        Sitter sitter = new Sitter();
        sitter.setId(10L);
        SitterVO vo = new SitterVO();
        vo.setId(10L);
        vo.setRealName("张三");

        when(sitterService.listPendingApplications()).thenReturn(List.of(sitter));
        when(sitterConverter.toVOList(List.of(sitter))).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/admin/sitter/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].realName").value("张三"));
    }
}
