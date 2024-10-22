package com.project.chatting.user.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LoginRepository {
    
    private static final String LOGIN_USERS = "LOGIN_USERS"; // 로그인 한 사용자들

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;

    // 로그인한 사용자의 목록 조회
    public List<String> getLoginUsers(){
        List<Object> loginUsers = redisTemplate.opsForList().range(LOGIN_USERS, 0, -1);
        if(loginUsers == null){
            return Collections.emptyList();
        }
        return loginUsers.stream().map(Object::toString).collect(Collectors.toList());
    }

    // 로그인 했을 경우 사용자 redis 세팅
    public void addLoginUsers(String userId){
        redisTemplate.opsForList().rightPush(LOGIN_USERS, userId);
    }

    // 로그아웃 시 redis 로그인 유저 삭제
    public void setLogoutUser(String userId){
        redisTemplate.opsForList().remove(LOGIN_USERS, 0, userId);
    }

    // 로그인 유저 목록 key 삭제
    public void removeLoginUsers(){
        redisTemplate.delete(LOGIN_USERS);
    }

}
