1.课程优势
开发方式前后端分离
服务端无状态
用户端实现为微信小程序

2.三大模块：
基础数据模块：
项目概述
环境搭建
员工管理
分类管理
菜品管理
套餐管理实战

点餐业务模块：
店铺营业状态设置
微信登录
缓存商品
购物车
用户下单
订单支付和管理
历史订单
订单状态定时处理
来单提醒和客户催单

统计报表模块
图形报表统计
Excel报表统计

3.学习收获
增长开发经验
提高业务分析能力
提高接口设计能力
提高编码能力
提高文档阅读能力（第三方服务对接）
提高代码调试能力

4.项目概述、环境搭建
-软件开发整体介绍
    1.软件开发流程：
        需求分析：需求规格说明书、产品原型（静态网页）
        设计：UI设计、数据库设计、接口设计
        编码：项目代码、单元测试
        测试：测试用例、测试报告
        上线运维：软件环境安装、配置
    2.角色分工
        项目经理:对整个项目负责，任务分配、把控进度
        产品经理:进行需求调研，输出需求调研文档、产品原型等（需求分析）
        UI设计师:根据产品原型输出界面效果图（设计）
        架构师:项目整体架构设计、技术选型等（设计）
        开发工程师:代码实现（编码）
        测试工程师:编写测试用例，输出测试报告（测试）
        运维工程师:软件环境搭建、项目上线（上线运维）
    3.软件环境
        开发环境：(development)开发人员在开发阶段使用的环境，一般外部用户无法访问
        测试环境：(testing)专门给测试人员使用的环境，用于测试项目，一般外部用户无法访问
        生产环境：(production)即线上环境，正式提供对外服务的环境
-苍穹外卖项目介绍
    项目介绍：
        定位：专门为餐饮企业定制的一款软件产品
        功能架构：体现项目中的业务功能模块
            管理端(/admin)：员工管理、订单管理、分类管理、工作台、菜品管理、数据统计、套餐管理、来单提醒
            用户端(/user)：微信登录、微信支付、商品浏览、历史订单、购物车、地址管理、用户下单、用户催单
    产品原型：用于展示项目的业务功能，一般由产品经理进行设计（用专业人眼见Axure等生成静态html）
    技术选型：展示项目中使用到的技术框架和中间件等
        用户层：node.js,VUE.js,ElementUI,微信小程序,apache echarts
        网关层：Nginx
        应用层：SpringBoot,SpringMVC,SpringTask,httpclient,SpringCache,JWT,阿里云OSS,Swagger,POI,WebSocket
        数据层：MySQL,Redis,mybatis,pagehelper,spring data redis
        工具：Git,maven,Junit,postman
-开发环境搭建
    前端：管理端（Web基于Nginx运行）& 用户端（小程序）
    后端：后端服务（Java基于maven项目构建并分模块开发）
    数据结构有文档，在D:\studyANDworkFiles\Graduate\coming-soon-resource\资料\day01\数据库
-导入接口文档（前后端分离前的接口定义）
-Swagger（后端接口自测）

5.后端前端联调
    controller：接受并封装参数、调用service方法查询数据库、封装结果并响应
    service：调用mapper查询数据库、密码比对、返回结果
    mapper：执行MySQL语句
    数据库

6.开发习惯，暂时没完成的工作或者idea记得用TODO

7.前后端分离开发流程&接口的重要性
    -定制接口（定义规范（前后端交互过程，地址，方式，数据格式等））
    -前后端并行开发（后端自测，mock数据）
    -连调（校验格式）
    -提测（自动化测试）