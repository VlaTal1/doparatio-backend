package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.UserBalanceDTO;
import com.vlatal.gitracker.serivce.JwtService;
import com.vlatal.gitracker.serivce.UserBalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserBalanceController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = com.vlatal.gitracker.config.SecurityConfig.class))
public class UserBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserBalanceService userBalanceService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    public void getBalance_success() throws Exception {
        UserBalanceDTO dto = UserBalanceDTO.builder()
                .userId("test-user-id")
                .balance(42)
                .build();

        when(userBalanceService.getBalance()).thenReturn(dto);

        mockMvc.perform(get("/api/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("test-user-id"))
                .andExpect(jsonPath("$.balance").value(42));
     }

    @Test
    public void subtractSeconds_success() throws Exception {
        UserBalanceDTO dto = UserBalanceDTO.builder()
                .userId("test-user-id")
                .balance(20)
                .build();

        when(userBalanceService.subtractSecondsForCurrentUser(10)).thenReturn(dto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/balance/subtract")
                        .param("seconds", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("test-user-id"))
                .andExpect(jsonPath("$.balance").value(20));
    }
}
