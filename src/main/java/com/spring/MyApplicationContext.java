package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {

    private Class config;
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public MyApplicationContext(Class config) {
        this.config = config;
        // 扫描
        scan(config);
        // 初始化单例bean
        initSingletonBeans();

    }

    private void initSingletonBeans() {
        BeanDefinition beanDefinition = null;
        for (String beanName : beanDefinitionMap.keySet()) {
            beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                singletonObjects.put(beanName, createBean(beanDefinition.getClazz()));
            }
        }
    }

    private void scan(Class config) {
        // 解析配置类
        ComponentScan componentScan = (ComponentScan) config.getDeclaredAnnotation(ComponentScan.class);
        String scanPath = componentScan.value().replace(".", "/");
        // 获取应用类加载器
        ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(scanPath);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if(f.getPath().endsWith("class")) {
                    String fpath = f.getPath();
                    String classPath = fpath.substring(fpath.indexOf("classes") + 8, fpath.indexOf(".class")).replace("\\", ".");
                    try {
                        Class clazz = classLoader.loadClass(classPath);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 表示当前这个类是一个Bean
                            Component component = (Component) clazz.getDeclaredAnnotation(Component.class);
                            String beanName = component.value();
                            // 解析类，判断当前bean是单例bean，还是prototype的bean
                            // BeanDefinition
                            BeanDefinition beanDefinition = new BeanDefinition();
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scope = (Scope) clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scope.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinition.setClazz(clazz);

                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if("prototype".equals(beanDefinition.getScope())) {
                return createBean(beanDefinition.getClazz());
            } else {
                return singletonObjects.get(beanName);
            }
        } else {
            throw new NullPointerException();
        }
    }

    private Object createBean(Class clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
