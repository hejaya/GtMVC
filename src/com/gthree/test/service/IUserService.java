package com.gthree.test.service;

import com.gthree.test.entity.User;

import java.util.List;

public interface IUserService {
    List<User> findAllUsers();
}
