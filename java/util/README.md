# java数据库操作代码
DataSourceMap类是读取配置文件进行初始化准备
JDBCDataSourceConfig类是执行数据库操作的工作类

# 使用的例子
public class MenuManagementJDBC {
    //UnitAuthorityDBTest数据库连接
    private static JDBCDataSourceConfig jdbcDataSourceConfig = null;

    static{
        jdbcDataSourceConfig = DataSourceMap.dataSource.get("UnitAuthorityDB");
    }
	
	/**
     * 查询系统清单
     * @return
     */
    public List<DOMenuSystemList> listSystemTable(){
        String sql = "SELECT SysId,SysName FROM dbo.t_SysProject";
        List<DOMenuSystemList> list = jdbcDataSourceConfig.select(sql, DOMenuSystemList.class);
        return list;
    }
}