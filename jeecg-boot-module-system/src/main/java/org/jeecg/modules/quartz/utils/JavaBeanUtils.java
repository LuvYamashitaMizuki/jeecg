package org.jeecg.modules.quartz.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class JavaBeanUtils {
        public static Object map2JavaBean(Class<?> clazz, Map<String, Object> map) throws Exception {
            Object javabean = clazz.newInstance(); // 构建对象
            Method[] methods = clazz.getMethods(); // 获取所有方法
            try {
                for (Method method : methods) {
                    if (method.getName().startsWith("set")) {
                        String field = method.getName(); // 截取属性名
                        field = field.substring(field.indexOf("set") + 3);
                        //对于setter方法，去掉set后并把去后的第一个大写的字母变成小写，就可以得到方法名
                        //不使用getter方法，是因为对于boolean来说，getter方法是isBoolean
                        field = field.toLowerCase().charAt(0) + field.substring(1);
                        if (map.containsKey(field)) {
                            //对id单独处理，因为id可能写成ID
                            if("id".equals(field)){
                                String id =map.get(field)+"";
                                method.invoke(javabean,id);
                            }else{
                                method.invoke(javabean, map.get(field));
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return javabean;
        }


        /**
         * obj-obj
         * @param clazz
         * @param obj
         * @return
         * @throws Exception
         */
        public static Object Bean2JavaBean(Class<?> clazz, Object obj) throws Exception {
            Object javabean = clazz.newInstance(); // 构建对象
            Method[] methods = clazz.getMethods(); // 获取所有方法
            try {
                for (Method method : methods) {
                    if (method.getName().startsWith("set")) {
                        String field = method.getName(); // 截取属性名
                        field = field.substring(field.indexOf("set") + 3);
                        field = field.toLowerCase().charAt(0) + field.substring(1);

                        Method[] desMethods = obj.getClass().getMethods();
                        for(Method desMethod :desMethods){
                            if(desMethod.getName().startsWith("set")){
                                String desField = desMethod.getName(); // 截取属性名
                                desField = desField.substring(desField.indexOf("set") + 3);
                                desField = desField.toLowerCase().charAt(0) + desField.substring(1);
                                if(desField.equalsIgnoreCase(field)){
                                    method.invoke(javabean, field);
                                }
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return javabean;
        }


}


