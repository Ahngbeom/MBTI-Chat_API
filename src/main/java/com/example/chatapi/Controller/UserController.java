package com.example.chatapi.Controller;

import com.example.chatapi.DTO.AuthorityDTO;
import com.example.chatapi.DTO.UserDTO;
import com.example.chatapi.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Transactional
    @PostConstruct
    public void setAdmin() throws RuntimeException {
        /*
            Spring Bean LifeCycle CallBack - @PostConstruct
            빈 생명주기 콜백: 스프링 빈이 생성된 후 의존관계 주입이 완료되거나 죽기 직전에 스프링 빈 안에 존재하는 메소드를 호출해주는 기능
            초기화 콜백 함수 setAdmin 함수를 추가하여 H2 데이터베이스에 Admin 계정을 등록한다.
         */
        try {
            AuthorityDTO authority = AuthorityDTO.builder()
                    .authorityName("ROLE_ADMIN")
                    .build();
            if (authorityRepository.save(authority).getClass() != Authority.class)
                throw new RuntimeException("ERROR SAVED ADMIN AUTHORITY ON AUTHORITY TABLE");

            authority = Authority.builder()
                    .authorityName("ROLE_USER")
                    .build();
            if (authorityRepository.save(authority).getClass() != Authority.class)
                throw new RuntimeException("ERROR SAVED USER AUTHORITY SAVE ON AUTHORITY TABLE");

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        log.info("SUCCESS SAVE ON AUTHORITY TABLE");
        try {
            Set<Authority> adminAuthorities = new HashSet<>();
            adminAuthorities.add(Authority.builder().authorityName("ROLE_ADMIN").build());
            adminAuthorities.add(Authority.builder().authorityName("ROLE_USER").build());

            User user = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .nickname("ADMIN")
                    .authorities(adminAuthorities)
                    .activated(true)
                    .build();
            if (userRepository.save(user).getClass() != User.class)
                throw new RuntimeException("ERROR SAVE ON USER TABLE");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        log.info("SUCCESS SAVE ON USERS TABLE");
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDTO userDTO) {
        if (userService.signUp(userDTO))
            return ResponseEntity.ok().body(null);
        else
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
    }

}
