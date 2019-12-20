package com.gthree.test.api;

import com.gthree.gtmvcframework.annotation.GtAutowired;
import com.gthree.gtmvcframework.annotation.GtController;
import com.gthree.gtmvcframework.annotation.GtRequestMapping;
import com.gthree.gtmvcframework.annotation.GtRequestParam;
import com.gthree.test.entity.User;
import com.gthree.test.service.IUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
//http://localhost:8081/GtMVC_war_exploded/user/users?name=zhangsan&age=111
@GtController
@GtRequestMapping("/user")
public class UserController {

    @GtAutowired
    private IUserService userService;

    @GtRequestMapping("/users")
    public void getAllUsers(@GtRequestParam(value = "name") String name, @GtRequestParam(value = "age") Integer age, HttpServletRequest request, HttpServletResponse response){
        System.out.println("接受请求参数===》" + name + age);
        List<User> users = userService.findAllUsers();
        System.out.println("users = " + users);
    }

    @GtRequestMapping("/deps")
    public void getAllUsers(HttpServletRequest request, HttpServletResponse response){
        System.out.println("查询部门");
    }
}
