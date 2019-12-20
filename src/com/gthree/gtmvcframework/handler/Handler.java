package com.gthree.gtmvcframework.handler;

import com.gthree.gtmvcframework.annotation.GtRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Handler {
    private Object controller;//类实例
    private Method method;//当前方法
    private Pattern pattern;//正则校验
    private Map<String, Integer> paramIndexMapping;//方法参数映射

    public Handler(Pattern pattern, Object controller, Method method){
        this.pattern = pattern;
        this.controller = controller;
        this.method = method;
        paramIndexMapping = new HashMap<String, Integer>();
        putParamIndexMapping(method);
    }

    /**
     * 将参数封装
     * @param method
     */
    private void putParamIndexMapping(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();//获取方法参数上的注解
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof GtRequestParam){//注解类型是GtRequestParam
                    String paraName = ((GtRequestParam) annotation).value();//获取注解参数名称
                    if (!"".equals(paraName)){
                        paramIndexMapping.put(paraName,i);//将参数和索引对应
                    }
                }
            }
        }

        Class<?>[] parameterTypes = method.getParameterTypes();//获取方法参数类型
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> type = parameterTypes[i];
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){//如果参数是响应或请求
                paramIndexMapping.put(type.getName(), i);
            }
        }

    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }
}
