package com.eatsfine.eatsfine.controller;

import com.eatsfine.eatsfine.global.config.DeployProperties;
import com.eatsfine.eatsfine.global.controller.HealthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = HealthController.class)
@EnableConfigurationProperties(DeployProperties.class)
@AutoConfigureMockMvc
@DisplayName("HealthController 중형 테스트: 스프링 MVC와 컨트롤러가 잘 결합하여 동작하는가?")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("healthCheck : 현재 서버가 살아있다면 200 상태코드와 함께 활성화된 프로필을 문자열로 응답한다")
    void healthCheckTest() throws Exception {
        mockMvc
                .perform(get("/api/v1/deploy/health-check"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType("text/plain;charset=UTF-8"),
                        content().string("test") // active profile set to 'test'
                );
    }
}
