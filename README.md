[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

产品介绍 <img height="50px" width="50px" src="https://github.com/Chris2018998/BeeCP/blob/master/doc/individual/bee.png"></img>
---
小蜜蜂是一款轻量级（15个文件，2000行源码）无锁高性能Java连接池

命名缘由
---
蜜蜂对人类是一种有益昆虫，近些年新闻报导其数量呈下降趋势，会直接影响到人类食物的产量，希望能引起更多朋友的关注和重视：蜜蜂虽小，作用很大，保护环境，关爱大自然。

Maven下载
---
Download<a href="http://central.maven.org/maven2/com/github/chris2018998/BeeCP/0.87/BeeCP-0.87.jar">BeeCP_0.87.jar</a>

```java
<dependency>
   <groupId>com.github.chris2018998</groupId>
   <artifactId>BeeCP</artifactId>
   <version>0.87</version>
</dependency>

```

配置说明
---
|  Name  |   Description |   Remark |
| ------------ | ------------ | ------------ |
| poolInitSize  |连接池初始大小  |   |
| poolMaxSize   |连接池最大个数  |    |
| maxWaitTime   |连接借用等待最大时间(毫秒)  |   |
| maxIdleTime   |连接闲置最大时间(毫秒)     |   |  
| preparedStatementCacheSize |SQL宣言缓存大小 |   
| validationQuerySQL |连接是否存活测试查询语句   |    |   |


使用参考
---
```java
String userId="root";
String password="";
String driver="com.mysql.jdbc.Driver";
String URL="jdbc:mysql://localhost/test";
BeeDataSourceConfig config = new BeeDataSourceConfig(driver,URL,userId,password);
DataSource datasource = new BeeDataSource(config);
Connection con = datasource.getConnection();
....................

```

性能测试
---
以多线程查询(1000个线程各自执行1000次，共100万次)的方式测试各连接池性能，并打印耗时分布以及平均耗时，最后依据平时耗时为各连接池进行名次排列，单次时间统计(机器状态对测试结果有较大影响)：

[datasource.getConnection(),connection.prepareStatement,statement.execute(),statement.close(),connection.close()]</i>

1：下面为各连接池在Oracle11G的下测试结果（单位：毫秒）

<a href="https://github.com/Chris2018998/BeeCP/blob/master/doc/performance/20190808_I5_4210M_Orcale11g.log">20190808_I5_4210M_Orcale11g.log</a>

Bee_C(30.7656) > HikariCP(38.3209) > Vibur(49.7906) > Bee_F(55.0636) > TOMCAT(121.8306) > Druid(126.8635) > DBCP(127.7240) > C3P0(144.3271)

2：以光连接池的驱动（专用于连接池性能测试的驱动）测试情况如下

<a href="https://github.com/Chris2018998/BeeCP/blob/master/doc/performance/I3_7100_HikariCP_Driver_20190729.log">I3_7100_HikariCP_Driver_20190729.log</a>

Bee_F(0.0006) > Bee_C(0.0980) > HikariCP(0.3053) > Vibur(0.3068) > TOMCAT(1.9001) > DBCP(3.9862) > C3P0(6.3528) > Druid(9.7170)

<a href="https://github.com/Chris2018998/BeeCP/blob/master/doc/performance/20190808_I5_4210M_HikariCP_Driver.log">20190808_I5_4210M_HikariCP_Driver.log</a>

Bee_C(0.0018) > Vibur(0.0048) > Bee_F(0.1982) > HikariCP(0.3832) > TOMCAT(2.3388) > Druid(3.0775) > DBCP(5.2606) > C3P0(11.9082)


性能测试代码请访问项目：https://github.com/Chris2018998/PoolPerformance


支持与联系 
---
Email:Chris2018998@tom.com

<img src="https://github.com/Chris2018998/BeeCP/blob/master/doc/individual/w.png"> </img>
<img src="https://github.com/Chris2018998/BeeCP/blob/master/doc/individual/z.png"> </img>

开发邀请
---
欢迎对连接池有兴趣的网友一起开发和维护
