package com.icode.securitydemo.security.service;


import com.icode.securitydemo.users.entity.UserEntity;
import com.icode.securitydemo.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public MyUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntity = userRepository.findByUsernameIgnoreCase(username.toLowerCase());
        userEntity.orElseThrow(() -> new UsernameNotFoundException("Not Found" + username));
        return new MyUserDetails(userEntity.get());
    }
}
