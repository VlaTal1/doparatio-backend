package com.vlatal.gitracker.serivce;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    public String getCurrentUserId() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> principal = (Map<String, String>) authentication.getPrincipal();

        if (principal == null) {
            throw new Exception("Unable to get userId");
        }

        return principal.get("personId");
    }
}
