package com.nextech.controller;

import com.nextech.service.CategoryService;
import com.nextech.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean ProductService productService;
    @MockBean CategoryService categoryService;

    @Test
    void getHome_returnsOkWithHomeView() throws Exception {
        given(productService.findApprovedFiltered(isNull(), isNull(), any()))
                .willReturn(new PageImpl<>(Collections.emptyList()));
        given(categoryService.findAll()).willReturn(List.of());
        given(productService.countByStatus(any())).willReturn(0L);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("featuredProducts", "categories", "totalProducts"));
    }
}
