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
     * 获得数据列表
     * @param strWhere
     * @return
     */
    public List<DOGetListTreeSelect> GetList(String strWhere){
        StringBuilder strSql = new StringBuilder();
        strSql.append("select t.TId,t.TabName,t.PageUrl,t.fatherId,t.IsDelete,t.IsCommon,t.SortCode,t.Icon,t.TabDerisc,t.Creater,t.CreateTime,t1.TabName as fatherName,s.SysId,s.SysName,kk.RoleName,kk.Rids  from t_TabInfo t ");
        strSql.append(" left join t_TabInfo t1 on t1.TId=t.fatherId");
        strSql.append(" left join t_SysTab st on t.TId=st.TId");
        strSql.append(" left join t_SysProject s on s.SysId=st.SysId");
        strSql.append(" left join  (select Tid,RoleName =(stuff( (select ',' + RoleName from (select a.TId,b.RoleName from dbo.t_TabAuthority a ,t_Role b where a.RId = b.Rid )as tb  where tb.Tid = A.Tid for xml path('')), 1,1,''))");
        strSql.append("  ,Rids =(stuff( (select ',' +cast(RId as varchar) from (select a.TId,a.RId from dbo.t_TabAuthority a ,t_Role b where a.RId = b.Rid ) as tb  where tb.Tid = A.Tid for xml path('')), 1,1,''))");
        strSql.append(" from (select a.TId,b.RoleName from dbo.t_TabAuthority a ,t_Role b where a.RId = b.Rid) as A group by Tid ) kk on t.TId = kk.TId ");
        //strSql.Append(" FROM t_TabInfo ");
        if (strWhere.trim() != "")
        {
            strSql.append(" where " + strWhere);
        }

		//下面是使用
        List<DOGetListTreeSelect> list = jdbcDataSourceConfig.select(strSql.toString(),DOGetListTreeSelect.class);
        return list;
    }
}

/**
* 传入对象,使用注解,由lib实现字段绑定
*/
public class DOGetListTreeSelect {
    @DataConversion("TId")
    private Integer id;
    @DataConversion("fatherId")
    private Integer parentId;
    @DataConversion("TabName")
    private String text;
    private Object data;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}