package com.example.demo8.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用JDBCDataSources.properties的值建立数据库连接后的操作对象
 */
public class JDBCDataSourceConfig {
    public Connection conUnitAuthorityDBTest;

    public JDBCDataSourceConfig(String con,String user,String pass){
        try {
            conUnitAuthorityDBTest = DriverManager.getConnection(con,user,pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从ResultSet中获取所有列所组合而成的Set<String>
     * @param resultSet
     * @return
     */
    private Set<String> ResultSetGetKey(ResultSet resultSet){
        //获取所有列的名称并保存为Set
        Set<String> set = new HashSet<>();
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int count = resultSetMetaData.getColumnCount();
            for(int i = 1; i <= count; i ++){
                set.add(resultSetMetaData.getColumnName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return set;
    }

    /**
     * 将ResultSet转化为List<Map<String,Object>>
     * @param resultSet
     * @return
     */
    private List<Map<String,Object>> ResultSetToListOfMap(ResultSet resultSet){
        List<Map<String,Object>> list = new ArrayList<>();
        try {
            //获取结果集的列集合
            Set<String> set = ResultSetGetKey(resultSet);
            //拿到了列的列表，直接遍历整个resultSet，并将值放入ListMap中
            while (resultSet.next()){
                Map<String,Object> map = new HashMap<>();
                for(String str:set){
                    Object obj = resultSet.getObject(str);
                    map.put(str,obj);
                }
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 将ResultSet转化为List<Object>,只有一列
     * @param resultSet
     * @return
     */
    private List ResultSetToList(ResultSet resultSet){
        List list = new ArrayList();
        try {
            while (resultSet.next()){
                Object object = resultSet.getObject(1);
                list.add(object);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private Object analysisAnnotation(DataConversion dataConversion, Object value){
        return value;
    }

    private Object parsingAnnotation(ProcessConversion ProcessConversion,Object value){
        Object object = null;
        try {
            Class c = ProcessConversion.obj();
            String methName = ProcessConversion.method();

            Object objm = c.newInstance();

            Method method = c.getDeclaredMethod(methName,Object.class);
            method.setAccessible(true);
            object = method.invoke(objm,value);


        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }  catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return object;
    }

    private Object assignmentField(Field field,Object object,Object value){
        field.setAccessible(true);
        try {
            field.set(object,value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 将ResultSet转化为List<T>，支持注解
     * @param resultSet
     * @param obj 传入转化对象的class
     * @return
     */
    private List resultSetToLisrtOfClass(ResultSet resultSet,Class obj){
        //如果obj和dataProcess为空
        if(obj == null){
            return ResultSetToListOfMap(resultSet);
        }

        List list = new ArrayList();
        try {

            //把列名称放入一个列表里
            Set<String> set = ResultSetGetKey(resultSet);
            while (resultSet.next()){
                //创建实例
                Object object = null;
                //获取字段列表对象
                Field[] fields =obj.getDeclaredFields();
                object = obj.newInstance();
                for (Field field : fields){
                    Object value = null;
                    DataConversion dataConversion = field.getAnnotation(DataConversion.class);
                    ProcessConversion ProcessConversion = field.getAnnotation(ProcessConversion.class);
                    if(dataConversion!=null){
                        //变量名字转换
                        String fieName = dataConversion.value();
                        value = analysisAnnotation(dataConversion,resultSet.getObject(fieName));
                        object = assignmentField(field,object,value);

                    }else if(ProcessConversion!=null){
                        //变量过程转换
                        String name = ProcessConversion.value();
                        value = parsingAnnotation(ProcessConversion,resultSet.getObject(name));
                        object = assignmentField(field,object,value);

                    }else{
                        //没有注解的情况转换
                        String fieName = field.getName();
                        if(set.contains(fieName)){
                            value = resultSet.getObject(fieName);
                            object = assignmentField(field,object,value);
                        }
                    }

                }
                list.add(object);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 将ResultSet转化为List<T>，支持注解,如果首字母为大写则转换为小写，否则保持原样
     * @param resultSet
     * @param obj 传入转化对象的class
     * @return
     */
    private List resultSetToLisrtOfClassBig(ResultSet resultSet,Class obj){
        //如果obj和dataProcess为空
        if(obj == null){
            return ResultSetToListOfMap(resultSet);
        }

        List list = new ArrayList();
        try {

            //把列名称放入一个列表里
            Set<String> set = ResultSetGetKey(resultSet);
            while (resultSet.next()){
                //创建实例
                Object object = null;
                //获取字段列表对象
                Field[] fields =obj.getDeclaredFields();
                object = obj.newInstance();
                for (Field field : fields){
                    Object value = null;
                    DataConversion dataConversion = field.getAnnotation(DataConversion.class);
                    ProcessConversion ProcessConversion = field.getAnnotation(ProcessConversion.class);
                    if(dataConversion!=null){
                        //变量名字转换
                        String fieName = dataConversion.value();
                        value = analysisAnnotation(dataConversion,resultSet.getObject(fieName));
                        object = assignmentField(field,object,value);

                    }else if(ProcessConversion!=null){
                        //变量过程转换
                        String name = ProcessConversion.value();
                        value = parsingAnnotation(ProcessConversion,resultSet.getObject(name));
                        object = assignmentField(field,object,value);

                    }else{
                        //没有注解的情况转换
                        String fieName = field.getName();
                        if(set.contains(fieName)){
                            String name = UppercaseToLowercase(fieName);
                            value = resultSet.getObject(name);
                            object = assignmentField(field,object,value);
                        }
                    }

                }
                list.add(object);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 根据sql语句直接查询结果并返回List<Object>,注意，sql语句的结果只能有一列
     * @param sql
     * @return
     */
    public List selectList(String sql){
        Statement statement = null;
        List<Object> list = null;
        ResultSet resultSet = null;
        try {
            statement = conUnitAuthorityDBTest.createStatement();
            resultSet = statement.executeQuery(sql);
            list = ResultSetToList(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 根据sql语句直接查询结果并返回ListT,(支持注解)
     * 传入的obj上，默认是将查询结果的字段名字和对象名字完全对应起来，
     * 如果对象的某一个名字在查询结果的列中找不到，则对象值保持初始值，
     * 不会填充数据给它。
     * 另外，对象属性支持几种注解，详情请参见当前包下的注解：
     * DataConversion，ProcessConversion
     * DataConversion注解的value属性是表示了属性和查询结果的列名称对应
     * ProcessConversion注解的value属性也是表示属性和列名称的对应，obj属性表示绑定哪一个处理对象，method表示方法名。
     * @param sql
     * @param obj 用来将列转化为对象
     * @return
     */
    public List select(String sql,Class obj){
        Statement statement = null;
        List list = null;
        ResultSet resultSet = null;
        try {
            //执行query，直接把结果转化为ListT
            statement = conUnitAuthorityDBTest.createStatement();
            resultSet = statement.executeQuery(sql);
            list = resultSetToLisrtOfClass(resultSet,obj);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 根据sql语句直接查询结果并返回ListT,(支持注解)
     * 传入的obj上，默认是将查询结果的字段名字和对象名字完全对应起来，
     * 如果对象的某一个名字在查询结果的列中找不到，则对象值保持初始值，
     * (该方法是select的大写版本，会判断查询结果列的首字母，如果为大写则转换为小写匹配，否则原样匹配)不会填充数据给它。
     * 另外，对象属性支持几种注解，详情请参见当前包下的注解：
     * DataConversion，ProcessConversion
     * DataConversion注解的value属性是表示了属性和查询结果的列名称对应
     * ProcessConversion注解的value属性也是表示属性和列名称的对应，obj属性表示绑定哪一个处理对象，method表示方法名。
     * @param sql
     * @param obj 用来将列转化为对象
     * @return
     */
    public List selectBig(String sql,Class obj){
        Statement statement = null;
        List list = null;
        ResultSet resultSet = null;
        try {
            //执行query，直接把结果转化为ListT
            statement = conUnitAuthorityDBTest.createStatement();
            resultSet = statement.executeQuery(sql);
            list = resultSetToLisrtOfClassBig(resultSet,obj);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 查询并返回第一行第一列
     * @param sql
     * @return
     */
    public Object selectFirstColumn(String sql){
        Statement statement = null;
        Object object = null;
        ResultSet resultSet = null;
        try {
            statement = conUnitAuthorityDBTest.createStatement();
            resultSet =  statement.executeQuery(sql);
            if(resultSet.next()){
                object = resultSet.getObject(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 查询并返回第一行第一列
     * @param sql
     * @return
     */
    public Object selectFirstColumnPar(String sql,Object[] parameter){
        PreparedStatement preparedStatement = null;
        Object object = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = conUnitAuthorityDBTest.prepareStatement(sql);
            for(int i = 0; i < parameter.length; i ++){
                preparedStatement.setObject(i+1,parameter[i]);
            }
            resultSet =  preparedStatement.executeQuery();
            if(resultSet.next()){
                object = resultSet.getObject(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 查询过后返回一条数据
     * @param sql 查询语句（sql格式）
     * @param obj 转化为指定对象，不能为空
     * @return
     */
    public Object selectOne(String sql,Class obj){
        Statement statement = null;
        Object object = null;
        ResultSet resultSet = null;
        try {
            statement = conUnitAuthorityDBTest.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                Field[] fields = obj.getDeclaredFields();

                for(Field field:fields){
                    field.setAccessible(true);
                    field.set(object,resultSet.getObject(field.getName()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 查询过后返回一条数据
     * @param sql 查询语句（sql格式）
     * @param obj 转化为指定对象，不能为空
     * @return
     */
    public Object selectOnePar(String sql,Class obj){
        Statement statement = null;
        Object object = null;
        ResultSet resultSet = null;
        try {
            statement = conUnitAuthorityDBTest.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                Field[] fields = obj.getDeclaredFields();

                for(Field field:fields){
                    field.setAccessible(true);
                    field.set(object,resultSet.getObject(field.getName()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 查询过后返回一条数据,如果查询数据头字母为大写则转换为小写，否则保持原样
     * @param sql 查询语句（sql格式）
     * @param obj 转化为指定对象，不能为空
     * @return
     */
    public Object selectOneBig(String sql,Class obj){
        Statement statement = null;
        Object object = null;
        ResultSet resultSet = null;
        try {
            statement = conUnitAuthorityDBTest.createStatement();
            resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                Field[] fields = obj.getDeclaredFields();

                for(Field field:fields){
                    field.setAccessible(true);
                    String name = UppercaseToLowercase(field.getName());
                    field.set(object,resultSet.getObject(name));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    private static Pattern pattern = Pattern.compile("[A-Z]");
    /**
     * 如果头字母为大写则转换为小写字母，否则返回原样字符串
     * @param capital
     * @return
     */
    private String UppercaseToLowercase(String capital){
        char ch = capital.charAt(0);
        String str = String.valueOf(ch);
        Matcher m = pattern.matcher(str);
        boolean bo = m.lookingAt();
        if(bo){
            str = str.toLowerCase();
        }
        capital = capital.substring(1,capital.length());
        return str + capital;
    }

    /**
     * 执行insert，update，delete语句的方法
     * @param sql sql语句
     * @return
     */
    public int ExecuteSql(String sql){
        Statement statement = null;
        int res = 0;
        try {
            statement = conUnitAuthorityDBTest.createStatement();
            res = statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     * 执行insert，update，delete语句的方法（防sql注入版本）
     * @param sql sql语句
     * @param parameter 插入的参数
     * @return
     */
    public int ExecuteSqlPar(String sql,Object[] parameter){
        return executeSql(sql,parameter);
    }

    /**
     * 执行insert，update，delete语句的方法（防sql注入版本）
     * @param sql sql语句
     * @param parameter 插入的参数
     * @return
     */
    public int ExecuteSqlParList(String sql,List parameter){
        return executeSqlList(sql,parameter);
    }

    /**
     * 数据库事务（执行多条sql语句）
     * @param sqlArray
     */
    public void TranExecuteSql(String[] sqlArray){
        Statement statement = null;
        try {
            //取消自动提交（开启事务）
            conUnitAuthorityDBTest.setAutoCommit(false);
            statement = conUnitAuthorityDBTest.createStatement();
            for(String sql:sqlArray){
                statement.executeUpdate(sql);
            }
            conUnitAuthorityDBTest.commit();
        } catch (SQLException e) {
            try {
                conUnitAuthorityDBTest.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                //关闭事务
                conUnitAuthorityDBTest.setAutoCommit(true);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 数据库事务（支持预编译的多条sql语句）
     * @param sqlArrayPar
     */
    public void TranExecuteSqlPar(Map<String,Object[]> sqlArrayPar){
        try {
            //开启事务
            conUnitAuthorityDBTest.setAutoCommit(false);
            for(Map.Entry<String,Object[]> sqlPar : sqlArrayPar.entrySet()){
                String sql = sqlPar.getKey();
                Object[] par = sqlPar.getValue();
                executeSql(sql,par);
            }
            conUnitAuthorityDBTest.commit();
        } catch (SQLException e) {
            try {
                conUnitAuthorityDBTest.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                conUnitAuthorityDBTest.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private int executeSql(String sql,Object[] par){
        PreparedStatement preparedStatement = null;
        int res = 0;
        try {
            preparedStatement = conUnitAuthorityDBTest.prepareStatement(sql);
            for(int i = 0; i < par.length; i ++){
                preparedStatement.setObject(i+1,par[i]);
            }
            res = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private int executeSqlList(String sql,List par){
        PreparedStatement preparedStatement = null;
        int res = 0;
        try {
            preparedStatement = conUnitAuthorityDBTest.prepareStatement(sql);
            for(int i = 0; i < par.size(); i ++){
                preparedStatement.setObject(i+1,par.get(i));
            }
            res = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

}
