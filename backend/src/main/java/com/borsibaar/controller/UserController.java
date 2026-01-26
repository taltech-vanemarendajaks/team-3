package com.borsibaar.controller;

import com.borsibaar.dto.UserSummaryResponseDto;
import com.borsibaar.entity.User;
import com.borsibaar.mapper.UserMapper;
import com.borsibaar.repository.UserRepository;
import com.borsibaar.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping
    public ResponseEntity<List<UserSummaryResponseDto>> getOrganizationUsers() {
        User admin = SecurityUtils.getCurrentAdmin();

        List<UserSummaryResponseDto> users = userRepository.findByOrganizationId(admin.getOrganizationId())
                .stream()
                .map(userMapper::toSummaryDto)
                .toList();

        return ResponseEntity.ok(users);
    }
}
