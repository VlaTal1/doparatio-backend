package com.vlatal.gitracker.serivce;

import com.vlatal.gitracker.bom.UserBalanceDTO;
import com.vlatal.gitracker.entity.UserBalance;
import com.vlatal.gitracker.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBalanceService {

    private final UserBalanceRepository userBalanceRepository;
    private final UserService userService;

    public UserBalanceDTO getBalance() throws Exception {
        String userId = userService.getCurrentUserId();
        UserBalance balance = getOrCreateBalance(userId);
        return UserBalanceDTO.builder()
                .userId(balance.getUserId())
                .balance(balance.getBalance())
                .build();
    }

    @Transactional
    public void addSeconds(String userId, int seconds) {
        if (seconds <= 0) {
            return;
        }
        UserBalance balance = getOrCreateBalance(userId);
        balance.setBalance(balance.getBalance() + seconds);
        userBalanceRepository.save(balance);
    }

    @Transactional
    public void subtractSeconds(String userId, int seconds) {
        if (seconds <= 0) {
            return;
        }
        UserBalance balance = getOrCreateBalance(userId);
        balance.setBalance(Math.max(0, balance.getBalance() - seconds));
        userBalanceRepository.save(balance);
    }

    @Transactional
    public UserBalanceDTO subtractSecondsForCurrentUser(int seconds) throws Exception {
        String userId = userService.getCurrentUserId();
        subtractSeconds(userId, seconds);
        return getBalance();
    }

    private UserBalance getOrCreateBalance(String userId) {
        return userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> userBalanceRepository.save(
                        UserBalance.builder()
                                .userId(userId)
                                .balance(0)
                                .build()
                ));
    }
}
