package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.UserBalanceDTO;
import com.vlatal.gitracker.repository.UserBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class UserBalanceServiceTest {

    @Autowired
    private UserBalanceService userBalanceService;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    public void setUp() throws Exception {
        when(userService.getCurrentUserId()).thenReturn("test-user-id");
        userBalanceRepository.deleteAll();
    }

    @Test
    public void getBalance_createsNewIfAbsent() throws Exception {
        UserBalanceDTO dto = userBalanceService.getBalance();
        assertThat(dto.getUserId()).isEqualTo("test-user-id");
        assertThat(dto.getBalance()).isEqualTo(0);
    }

    @Test
    public void addSeconds_success() throws Exception {
        userBalanceService.addSeconds("test-user-id", 15);
        UserBalanceDTO dto = userBalanceService.getBalance();
        assertThat(dto.getBalance()).isEqualTo(15);
    }

    @Test
    public void subtractSeconds_success() throws Exception {
        userBalanceService.addSeconds("test-user-id", 30);
        userBalanceService.subtractSeconds("test-user-id", 10);
        UserBalanceDTO dto = userBalanceService.getBalance();
        assertThat(dto.getBalance()).isEqualTo(20);
    }

    @Test
    public void subtractSeconds_dontGoBelowZero() throws Exception {
        userBalanceService.addSeconds("test-user-id", 5);
        userBalanceService.subtractSeconds("test-user-id", 10);
        UserBalanceDTO dto = userBalanceService.getBalance();
        assertThat(dto.getBalance()).isEqualTo(0);
    }

    @Test
    public void subtractSecondsForCurrentUser_success() throws Exception {
        userBalanceService.addSeconds("test-user-id", 100);
        UserBalanceDTO dto = userBalanceService.subtractSecondsForCurrentUser(40);
        assertThat(dto.getBalance()).isEqualTo(60);
        assertThat(dto.getUserId()).isEqualTo("test-user-id");
    }

    @Test
    public void subtractSecondsForCurrentUser_negativeOrZeroIgnored() throws Exception {
        userBalanceService.addSeconds("test-user-id", 50);
        
        // Zero seconds should not change balance
        UserBalanceDTO dto = userBalanceService.subtractSecondsForCurrentUser(0);
        assertThat(dto.getBalance()).isEqualTo(50);
        
        // Negative seconds should not change balance
        dto = userBalanceService.subtractSecondsForCurrentUser(-5);
        assertThat(dto.getBalance()).isEqualTo(50);
    }
}
