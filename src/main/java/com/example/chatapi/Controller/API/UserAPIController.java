package com.example.chatapi.Controller.API;

import com.example.chatapi.DTO.AuthorityDTO;
import com.example.chatapi.DTO.UserDTO;
import com.example.chatapi.Entity.User.UserEntity;
import com.example.chatapi.Service.User.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAPIController {

	private final UserService userService;

	List<String> forbiddenWords = new ArrayList<>(Arrays.asList("administrator", "admin", "manager", "user", "server", "test", "tester"));

	@GetMapping("/info")
	public ResponseEntity<?> userInfo(Principal principal) {
		try {
			if (principal == null)
				throw new AuthenticationException("Not Logged In");
			return ResponseEntity.ok(userService.getUserInfo(principal.getName()));
		} catch (AuthenticationException authenticationException) {
			log.error(authenticationException.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authenticationException.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@Valid @RequestBody UserDTO userDTO) {
		UserEntity userEntity = null;
		try {
//			log.info("=== User information to be registered to register as a member ===");
//			log.info("User: " + userDTO.toString());
//			log.info("User's Authority: " + userDTO.getAuthorities());
			return ResponseEntity.ok().body(userService.signUp(userDTO));
		} catch (RuntimeException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@GetMapping("/list")
	public ResponseEntity<List<UserDTO>> userList() {
		try {
			List<UserDTO> userDTOList = userService.getUserList();
//			for (UserDTO user : userDTOList) {
//				log.info(user.getUsername());
//			}
			return ResponseEntity.ok(userDTOList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/authorities")
	public ResponseEntity<List<AuthorityDTO>> userAuthorities(Authentication authentication) {
		try {
			return ResponseEntity.ok(authentication.getAuthorities().stream()
					.map(grantedAuthority
							-> AuthorityDTO.builder().authorityName(grantedAuthority.getAuthority()).build())
					.collect(Collectors.toList()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@GetMapping("/authorities/{username}")
	public ResponseEntity<List<AuthorityDTO>> userAuthoritiesByAdmin(@PathVariable String username) {
		try {
			return ResponseEntity.ok(userService.getUserAuthorities(username));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
	}

	@GetMapping("/username-validation")
	public ResponseEntity<?> usernameValidation(@RequestParam String username) throws RuntimeException {
		try {
			forbiddenWords.forEach(word -> {
				if (username.toLowerCase().contains(word))
					throw new RuntimeException("Username contains forbidden words - \"" + word + "\"");
			});
			userService.getUserInfo(username);
		} catch (UsernameNotFoundException e) {
//			e.printStackTrace();
//			log.info("Username \"" + username + "\" is " + e.getMessage() + " (" + e.getClass() + ")");
			return ResponseEntity.ok(true);
		} catch (RuntimeException forbidden) {
//			forbidden.printStackTrace();
//			log.error(forbidden.getMessage());
			return ResponseEntity.ok(forbidden.getMessage());
		}
		log.error("Username \"" + username + "\" already exists");
		return ResponseEntity.ok("Username \"" + username + "\" already exists");
	}

	@GetMapping("/nickname-validation")
	public Boolean nicknameValidation(@RequestParam String nickname) {
		try {
			forbiddenWords.forEach(word -> {
				if (nickname.toLowerCase().contains(word))
					throw new RuntimeException("Nickname contains forbidden words - \"" + word + "\"");
			});
			return userService.nicknameValidation(nickname);
		} catch (Exception e) {
//			e.printStackTrace();
			log.info("Nickname \"" + nickname + "\" is " + e.getMessage() + " (" + e.getClass() + ")");
		}
		return false;
	}
}
