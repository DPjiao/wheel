package com.example.demo8.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * 数据库总舵
 */
public class DataSourceMap {
    /**
     * 所有的连接，都在这个属性里
     * 使用JDBCDataSources.properties这个配置文件的键可以作为key来取到对应的value(value是JDBCDataSourceConfig对象)
     */
    public static Map<String,JDBCDataSourceConfig> dataSource = new HashMap<>();

    static{
        try {
            //将数据载入配置类
            Properties properties = new Properties();
            //读取当前文件夹下的JDBCDataSources.properties文件
            Enumeration enumeration = getEnumeration(properties);


            //分类数据库连接列表的环境准备
            Map<String,ConnectionObject> map = new HashMap<>();
            List<String> list = new ArrayList<>();
            String key = null;
            String value = null;

            //遍历并循环键值对
            while (enumeration.hasMoreElements()){
                key = (String)enumeration.nextElement();
                value = properties.getProperty(key);

                String[] strings = key.split("\\.");
                String conValue = strings[0];
                if(map.get(conValue) == null){
                    //map里还没有这个key
                    ConnectionObject connectionObject = new ConnectionObject();
                    map.put(conValue,connectionObject);
                    list.add(conValue);
                    //更新指定key的value对象里的值
                    map = getStringConnectionObjectMap(map, value, strings, conValue);
                }else{
                    //map里有这个key
                    //更新指定key的value对象里的值
                    map = getStringConnectionObjectMap(map, value, strings, conValue);
                }
            }
            //遍历对象建立连接
            for(String str:list){
                traverseConnection(map, str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, ConnectionObject> getStringConnectionObjectMap(Map<String, ConnectionObject> map, String value, String[] strings, String conValue) {
        if (strings.length == 1) {
            map = setCon(map, conValue, value);
        } else {
            if (strings[1].equals("username")) {
                map = setUsername(map, conValue, value);
            } else if (strings[1].equals("password")) {
                map = setPassword(map, conValue, value);
            }
        }
        return map;
    }

    private static Map<String,ConnectionObject> setUsername(Map<String,ConnectionObject> map,String conValue,String value){
        ConnectionObject connectionObject = map.get(conValue);
        connectionObject.setUsername(value);
        map.put(conValue,connectionObject);
        return map;
    }
    private static Map<String,ConnectionObject> setPassword(Map<String,ConnectionObject> map,String conValue,String value){
        ConnectionObject connectionObject = map.get(conValue);
        connectionObject.setPassword(value);
        map.put(conValue,connectionObject);
        return map;
    }
    private static Map<String,ConnectionObject> setCon(Map<String,ConnectionObject> map,String conValue,String value){
        ConnectionObject connectionObject = map.get(conValue);
        connectionObject.setCon(value);
        map.put(conValue,connectionObject);
        return map;
    }

    private static void traverseConnection(Map<String, ConnectionObject> map, String str) {
        ConnectionObject connectionObject = map.get(str);
        String con1 = connectionObject.getCon();
        String username = connectionObject.getUsername();
        String password = connectionObject.getPassword();
        //用值创建数据库连接，并将连接和键关联起来，写入map类
        JDBCDataSourceConfig jdbcDataSourceConfig = new JDBCDataSourceConfig(con1,username,password);
        dataSource.put(str,jdbcDataSourceConfig);
    }

    private static Enumeration getEnumeration(Properties properties) throws IOException {
        properties.load(DataSourceMap.class.getClassLoader().getResourceAsStream("JDBCDataSources.properties"));
        return properties.propertyNames();
    }

}
