package com.wenxu.controller;

import com.wenxu.common.BaseContext;
import com.wenxu.converter.PetInfoConverter;
import com.wenxu.entity.PetInfo;
import com.wenxu.exception.GlobalExceptionHandler;
import com.wenxu.service.PetInfoService;
import com.wenxu.vo.PetInfoVO;
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

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetInfoControllerTest {

    @Mock
    private PetInfoService petInfoService;

    @Mock
    private PetInfoConverter petInfoConverter;

    @InjectMocks
    private PetInfoController petInfoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(100L);
        mockMvc = MockMvcBuilders.standaloneSetup(petInfoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void addPetShouldUseCurrentUser() throws Exception {
        mockMvc.perform(post("/api/pet/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petName\":\"小福\",\"petType\":2,\"breed\":\"柯基\",\"weight\":10.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("添加宠物成功"));

        verify(petInfoService).addPet(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(100L));
    }

    @Test
    void listMyPetsShouldReturnPetVOList() throws Exception {
        PetInfo pet = new PetInfo();
        pet.setId(10L);
        PetInfoVO vo = new PetInfoVO();
        vo.setId(10L);
        vo.setPetName("小福");

        when(petInfoService.listMyPets(100L)).thenReturn(List.of(pet));
        when(petInfoConverter.toVOList(List.of(pet))).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/pet/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].petName").value("小福"));
    }

    @Test
    void deletePetShouldUseCurrentUser() throws Exception {
        when(petInfoService.deleteMyPet(10L, 100L)).thenReturn(true);

        mockMvc.perform(delete("/api/pet/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("删除成功"));

        verify(petInfoService).deleteMyPet(10L, 100L);
    }
}
