package com.project.chatting.user.controller;


import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.chatting.auth.RefreshToken;
import com.project.chatting.chat.request.ChatRequest;
import com.project.chatting.chat.response.ChatResponse;
import com.project.chatting.common.ApiResponse;
import com.project.chatting.user.entity.User;
import com.project.chatting.user.request.SignupRequest;
import com.project.chatting.user.request.signinRequest;
import com.project.chatting.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired 
	private UserService userService;
	
	@Autowired 
	private PasswordEncoder encoder;
	
	/**
	 * 사용자등록
	 * @throws Exception 
	 */
	@Operation(summary = "회원가입")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "OK, 회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "C007",description = "아이디 중복"),
    })
	@PostMapping("/auth/signup")
	public ApiResponse<String> registerUser(@Valid @RequestBody SignupRequest signUpReq) {
	

		// Create new user's account
		User user = new User(signUpReq.getUserId(), encoder.encode(signUpReq.getUserPw()),  signUpReq.getName());

		userService.setInsertMember(user);

		return ApiResponse.SUCCESS;
	}
	
	/**
	 * 로그인 처리 - 추후 만들예정
	 * @throws Exception 
	 */
	
	@Operation(summary = "로그인")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",description = "OK, 로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "Error Code",description = "Error message",
                    content = @Content(schema = @Schema(implementation = RefreshToken.class))),
    })
	@PostMapping("/auth/signin")
	public ApiResponse<RefreshToken> loginUser(@Valid @RequestBody signinRequest signinReq, HttpServletResponse response){
		System.out.println("UserInfo" + signinReq);
	
		return ApiResponse.success(userService.login(signinReq, response));
		
	}
	
	/**
	 * 로그아웃
	 */
	@PostMapping("/auth/signout")
	public ApiResponse<String> logoutUser(HttpServletRequest req) {
		userService.logout(req);
		
		return ApiResponse.SUCCESS;
	}
	
	/**
	 * 사용자 목록 조회
	 */
	@GetMapping("/list")
	public ApiResponse<Map<String, Object>> getUserList(HttpServletRequest req) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("list", userService.getSortedUserList());
		
		return ApiResponse.success(result);
	}

	/**
	 * 로그인 상태 관리를 위한 웹 소켓 통신 컨트롤러
	 */
	@MessageMapping("/login")
    @SendTo("/sub/login")
	public ApiResponse<List<String>> sendMessage(@RequestBody signinRequest signinReq) {
		System.out.println("로그인 했을 때 소켓 연결 요청 파라미터 : " + signinReq);

		if(signinReq.getType() != null && signinReq.getType().equals("LOGOUT")){
			// 로그아웃 시 redis 갱신
			System.out.println("로그아웃 시 실행");
			userService.setLogoutUser(signinReq.getUserId());


			return ApiResponse.success(userService.setLogoutUser(signinReq.getUserId()));
		}
		return ApiResponse.success(userService.getLoginUsers(signinReq.getUserId()));
	}

	
}

