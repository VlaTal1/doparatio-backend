package com.vlatal.gitracker.controller;

import com.vlatal.gitracker.bom.UserBalanceDTO;
import com.vlatal.gitracker.serivce.UserBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class UserBalanceController {

    private final UserBalanceService userBalanceService;

    @GetMapping
    public ResponseEntity<UserBalanceDTO> getBalance() throws Exception {
        return ResponseEntity.ok(userBalanceService.getBalance());
    }

    @PostMapping("/subtract")
    public ResponseEntity<UserBalanceDTO> subtractMinutes(@RequestParam int minutes) throws Exception {
        return ResponseEntity.ok(userBalanceService.subtractMinutesForCurrentUser(minutes));
    }
}
