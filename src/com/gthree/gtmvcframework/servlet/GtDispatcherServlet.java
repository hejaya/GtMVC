package com.gthree.gtmvcframework.servlet;

import com.gthree.gtmvcframework.annotation.GtAutowired;
import com.gthree.gtmvcframework.annotation.GtController;
import com.gthree.gtmvcframework.annotation.GtRequestMapping;
import com.gthree.gtmvcframework.annotation.GtService;
import com.gthree.gtmvcframework.handler.Handler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GtDispatcherServlet extends HttpServlet {

    //存放配置文件
    private Properties contextConfig = new Properties();

    //存储所有扫描包全路径
    private List<String> classNames = new ArrayList<String>();

    //IOC容器，存放实例
    private Map<String, Object> ioc = new HashMap<String, Object>();

    //存放映射参数信息
    private List<Handler> handlerMapping = new ArrayList<Handler>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        startDispatcher(req, resp);//分发请求
    }

    /**
     * 分发请求
     *
     * @param req
     * @param resp
     */
    private void startDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        Handler handler = getHandler(req);
        try {
            if (handler == null) {
                resp.getWriter().write("404");
                return;
            }
            Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
            Object[] parameteValues = new Object[parameterTypes.length];//存储参数值

            Map<String, String[]> parameterMap = req.getParameterMap();//从请求取得所有参数

            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "");
                if (!handler.getParamIndexMapping().containsKey(entry.getKey())) {//注解映射里面是否包含请求的参数名
                    continue;
                }
                int index = handler.getParamIndexMapping().get(entry.getKey());
                parameteValues[index] = convert(parameterTypes[index], value);//数字类型转换
            }

            //判断是否注入 HttpServletRequest  HttpServletResponse
            if (handler.getParamIndexMapping().containsKey(HttpServletRequest.class.getName())){
                int reqIndex = handler.getParamIndexMapping().get(HttpServletRequest.class.getName());
                parameteValues[reqIndex] = req;
            }

            if (handler.getParamIndexMapping().containsKey(HttpServletResponse.class.getName())){
                int respIndex = handler.getParamIndexMapping().get(HttpServletResponse.class.getName());
                parameteValues[respIndex] = resp;
            }

            //反射执行controller方法
            handler.getMethod().invoke(handler.getController(), parameteValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) {
            return null;
        }
        String requestURL = req.getRequestURI();//请求url 如 /GtMVC_war_exploded/user/users/
        String contextPath = req.getContextPath();//项目路径 如 /GtMVC_war_exploded

        requestURL = requestURL.replace(contextPath, "");

        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(requestURL);//匹配正则 与mapping里面的对比
            if (!matcher.matches()) {
                continue;
            }
            return handler;//返回handler
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //加载配置文件
        loadConfig(config.getInitParameter("contextConfigLocation"));
        //扫描包
        scanPackage(contextConfig.getProperty("scanPackage"));
        //初始化
        startInstance();
        //开始自动注入
        startAutoWired();
        //开始路径映射
        startHandlerMapping();
    }

    /**
     * 加载配置文件
     *
     * @param contextConfigLocation 文件路径
     */
    private void loadConfig(String contextConfigLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(inputStream); //加载配置文件
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描包路径
     *
     * @param scanPackage 包路径
     */
    private void scanPackage(String scanPackage) {
        System.out.println("扫描包 = " + scanPackage);
        URL resource = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        System.out.println("扫描路径 = " + resource);
        File classFile = new File(resource.getFile());

        for (File file : classFile.listFiles()) {
            if (file.isDirectory()) {//如果是文件夹
                scanPackage(scanPackage + "." + file.getName());//递归扫描下级
            } else {
                //拼装全限定类名，加入集合
                String className = (scanPackage + "." + file.getName()).replace(".class", "");
                classNames.add(className);
                System.out.println("加入集合 = " + className);
            }
        }
    }

    /**
     * 初始化
     */
    private void startInstance() {
        if (classNames.isEmpty()) return;//未扫描，不执行
        try {
            for (String className : classNames) {
                Class<?> classz = Class.forName(className);//根据类名反射获取属性
                if (classz.isAnnotationPresent(GtController.class)) {//包含GtController注解
                    String beanName = lowerFirstCase(classz.getSimpleName());//类名小写  如 UserService ---->userService
                    ioc.put(beanName, classz.newInstance());//放入IOC容器 小写类名为键，实例为值
                } else if (classz.isAnnotationPresent(GtService.class)) {//包含GtService注解

                    GtService gtService = classz.getAnnotation(GtService.class);//获取service实例
                    String beanName = gtService.value();
                    if ("".equals(beanName)) {
                        beanName = lowerFirstCase(classz.getSimpleName());//如果service注解没有值，则默认取类名小写
                    }
                    Object newInstance = classz.newInstance();
                    ioc.put(beanName, newInstance);//放入IOC 实现类

                    Class<?>[] interfaces = classz.getInterfaces(); //获得这个对象所实现的接口。
                    for (Class<?> anInterface : interfaces) {
                        ioc.put(anInterface.getName(), newInstance); //放入IOC 接口  ---> 实现类
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAutoWired() {

        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();//获取类的属性
            for (Field field : fields) {
                if (!field.isAnnotationPresent(GtAutowired.class)) {//不包含autowired注解
                    continue;
                }
                GtAutowired autowired = field.getAnnotation(GtAutowired.class);
                String beanName = autowired.value();
                if ("".equals(beanName)) { //Autowired注解没有指定名称
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);//安全检查，可访问私有字段
                try {
                    field.set(entry.getValue(), ioc.get(beanName));//对象的字段设置为指定的新值.
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * 路径映射
     */
    private void startHandlerMapping() {
        if (ioc.isEmpty()) return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(GtController.class)) {//只处理controller
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(GtRequestMapping.class)) {//类上有GtRequestMapping注解
                GtRequestMapping annotation = clazz.getAnnotation(GtRequestMapping.class);
                baseUrl = annotation.value();//类上的根映射路径
            }

            Method[] methods = clazz.getMethods();//获取当前类的所有方法
            for (Method method : methods) {
                if (!method.isAnnotationPresent(GtRequestMapping.class)) {//当前方法没有GtRequestMapping注解
                    continue;
                }
                GtRequestMapping annotation = method.getAnnotation(GtRequestMapping.class);
                String url = (baseUrl + annotation.value()).replaceAll("/+", "/");//拼接路径

                Pattern compile = Pattern.compile(url);//创建一个匹配模式,后续与页面请求做对比
                handlerMapping.add(new Handler(compile, entry.getValue(), method));
                System.out.println("路径映射" + url + "=>" + method);
            }
        }
    }

    /**
     * 第一个字符转化为小写
     *
     * @param str
     * @return
     */
    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    /**
     * 类型转换 int 包装类型
     * @param type
     * @param value
     * @return
     */
    private Object convert(Class<?> type, String value) {
        if (Integer.class == type) {
            return Integer.valueOf(value);
        }
        return value;
    }

}
