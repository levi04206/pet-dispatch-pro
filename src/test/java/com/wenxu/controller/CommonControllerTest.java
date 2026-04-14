package com.wenxu.controller;

import com.wenxu.utils.AliOssUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommonControllerTest {

    @Mock
    private AliOssUtil aliOssUtil;

    @InjectMocks
    private CommonController commonController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commonController).build();
    }

    @Test
    void uploadShouldReturnUploadedUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "proof.jpg", "image/jpeg", "fake".getBytes());
        when(aliOssUtil.upload(file.getBytes(), "proof.jpg")).thenReturn("https://example.com/proof.jpg");

        mockMvc.perform(multipart("/api/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("https://example.com/proof.jpg"));
    }

    @Test
    void uploadShouldRejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "proof.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/common/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("上传文件不能为空"));
    }
}
