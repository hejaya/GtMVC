package com.gthree.test.service.impl;

import com.gthree.gtmvcframework.annotation.GtService;
import com.gthree.test.entity.User;
import com.gthree.test.service.BaseService;
import com.gthree.test.service.IUserService;

import java.util.ArrayList;
import java.util.List;

@GtService
public class IUserServiceImpl extends BaseService implements IUserService {

    @Override
    public List<User> findAllUsers() {
        ArrayList<User> users = new ArrayList<>();
        users.add(new User(1,"张三","123456"));
        users.add(new User(2,"李四","123456"));
        users.add(new User(3,"王五","123456"));
        return users;
    }

}
