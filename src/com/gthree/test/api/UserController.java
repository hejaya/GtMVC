package com.gthree.test.api;

import com.gthree.gtmvcframework.annotation.GtAutowired;
import com.gthree.gtmvcframework.annotation.GtController;
import com.gthree.gtmvcframework.annotation.GtRequestMapping;
import com.gthree.gtmvcframework.annotation.GtRequestParam;
import com.gthree.test.entity.User;
import com.gthree.test.service.IUserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
//http://localhost:8081/GtMVC_war_exploded/user/users?name=zhangsan&age=111
@GtController
@GtRequestMapping("/user")
public class UserController {

    @GtAutowired
    private IUserService userService;

    @GtRequestMapping("/users")
    public void getAllUsers(@GtRequestParam(value = "name") String name, @GtRequestParam(value = "age") Integer age, HttpServletResponse response) throws IOException {
        System.out.println("接受请求参数:" + name + age);
        List<User> users = userService.findAllUsers();

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(users.toString());
    }

    @GtRequestMapping("/deps")
    public void getAllUsers(){
        System.out.println("查询部门");
    }
}
