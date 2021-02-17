package com.icode.securitydemo.users.service;

import com.icode.securitydemo.users.entity.UserEntity;

public interface UserService {

    UserEntity addUser(UserEntity userEntity);

    void updateUser(UserEntity userEntity);

    int getUserId(String username);

    void deleteUser(int id);
}
