package com.icode.securitydemo.users.controller;


import com.icode.securitydemo.security.model.UserAuthenticateRequest;
import com.icode.securitydemo.security.model.UserAuthenticateResponse;
import com.icode.securitydemo.security.service.MyUserDetails;
import com.icode.securitydemo.security.service.MyUserDetailsService;
import com.icode.securitydemo.security.util.JwtUtil;
import com.icode.securitydemo.users.entity.UserEntity;
import com.icode.securitydemo.users.service.UserService;
import io.jsonwebtoken.impl.DefaultClaims;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserEntity userEntity){
        UserEntity addedUser;
        try{
            addedUser = userService.addUser(userEntity);
        }catch (ConstraintViolationException e){
            return ResponseEntity.badRequest().body("User or email already exists");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Entity is not valid");
        }

        final MyUserDetails userDetails = myUserDetailsService.loadUserByUsername(addedUser.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new UserAuthenticateResponse(jwt));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody UserAuthenticateRequest userAuthenticateRequest) throws Exception{
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userAuthenticateRequest.getUsername(), userAuthenticateRequest.getPassword()));
        } catch (BadCredentialsException e){
            return ResponseEntity.status(403).body("Incorrect Username or Password");
        }
        final MyUserDetails userDetails = myUserDetailsService.loadUserByUsername(userAuthenticateRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        final int id = userService.getUserId(userAuthenticateRequest.getUsername());
        return ResponseEntity.ok(new UserAuthenticateResponse(jwt));
    }

    @PutMapping("/signup")
    public ResponseEntity updateUser(@Valid @RequestBody UserEntity userEntity){
        try{
            userService.updateUser(userEntity);
            return ResponseEntity.ok().body("User Updated ");
        }catch (ConstraintViolationException e){
            return ResponseEntity.badRequest().body("User or email already exists");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Entity is not valid");
        }
    }

    @GetMapping("/refreshtoken")
    public ResponseEntity refreshToken(HttpServletRequest request){
        try{

            DefaultClaims claims = (DefaultClaims) request.getAttribute("claims");
            String username = claims.getSubject();
            final MyUserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
            final String jwt = jwtUtil.generateRefreshToken(userDetails);
            final int id = userService.getUserId(username);
            return ResponseEntity.ok(new UserAuthenticateResponse(jwt));
        }catch (NullPointerException e){
            return ResponseEntity.badRequest().body(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/test")
    public String test(){
        return "It is all okay you have access";
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable int userId){
        userService.deleteUser(userId);
    }
}
