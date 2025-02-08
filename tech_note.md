只关注后端，前端和接口文档用的现成的

sky-take-out：maven父工程，统一管理依赖版本，聚合其他子模块
sky-common：子模块，存放公共类，例如:工具类、常量类、异常类等
sky-pojo：子模块，存放实体类、VO、DTO等
    Entity-实体，通常和数据库中的表对应
    DTO-数据传输对象，通常用于程序中各层之间传递数据
    VO-视图对象，为前端展示数据提供的对象
    POJO-普通Java对象，只有属性和对应的getter和setter
sky-server：子模块，后端服务，存放配置文件、Controller、Service、Mapper等 

学会了断点调试，debug模式运行，打断点，可以一步一步走，也可以找下一个断点然后直接运行到断点位置step in，然后左侧可以看对应的参数传递过程中的变化，可以用来开发和debug的关键技巧，已可以用来跟踪函数分析代码

mapper写sql语句注意：比较简单的可以直接用注解，复杂的或者动态的sql（if，where，set，for-each都是mybatis的动态标签）就把sql配置到xml映射文件中

异常处理方式：抛出异常的时候由GlobalExceptionHandler统一捕获，然后return Result.error(ex.getMessage())给前端返回一个结果

像login生成token，jwt令牌啥的，通过配置项传递application.yml

可以加到简历上说的使用Nginx解决xxx：
前端发送的请求，是如何请求到后端服务的？
前端请求的形式类似request URL：http://localhost/api/employee/login
后端的接口地址类似:http://localhost:8080/admin/employee/login
8080是tomcat内嵌的
流程可以通过浏览器network跟踪，发现接口确实没有匹配但是请求成功了
是因为nginx反向代理，将前端发送的动态请求由nginx转发到后端服务器，nginx不仅仅可以做http服务器，还可以做反向代理服务器
实际的过程：浏览器将登录请求http://localhost/api/employee/login发送给Nginx，Nginx将http://localhost:8080/admin/employee/login反向代理转发给后端。
为什么这么做？
1.提高访问速度，在请求nginx的时候在这一层可以做缓存，再请求同样的接口地址就不用再跑后端了
2.Nginx充当负载均衡器，大量请求按照指定的方式均衡的分配给集群中的每台服务器
3.保证后端服务安全，后端服务端口不暴露给互联网，让前端不能直接请求到后端

-----------------------------------------------------------

Nginx使用反向代理和负载均衡要怎么配置？
见：nginx-1.20.2\conf\nginx.conf
1.反向代理形如
location /api/ {
    proxy_pass http://localhost:8080/admin/; //反向代理关键字
}
发过来的请求能匹配上api的字符串，nginx就通过反向代理将请求转发到后端指定地址
例如：用户http://localhost/api/employee/login -》 Nginx http://localhost:8080/admin/employee/login
2.负载均衡（基于反向代理）配置形如
upstream webservers{
    server 192/168.100.128:8080;
    server 192.168.100.129:8080;
}
server{
    listen 80;
    server_name localhost;

    location /api/ {
        proxy_pass http://webservers/admin/; #负载均衡
    }
}
相较于反向代理部分加了一个webservers（一个自定义的域名变量，可以叫别的名字但是上下要保持一致），定义了不同服务器的域名，声明一组服务器，前端的请求如果能匹配上，就由负载均衡平均的转发到不同的服务武器，分配机制默认为轮询，但是如果想添加策略，策略如下：
    1.轮询：默认方式
    2.weight：权重方式，默认为1，权重越高，被分配的客户端请求就越多
    3.ip_hash：依据ip分配方式，这样每个访客可以固定访问一个后端服务
    4.least conn：依据最少连接方式，把请求优先分配给连接数少的后端服务
    5.url hash：依据url分配方式，这样相同的url会被分配到同一个后端服务
    6.fair：依据响应时间方式，响应时间短的服务将会被优先分配

-----------------------------------------------------------

用户信息明文存储 -> MD5算法加密（不可逆）
流程更新：修改数据库中铭文密码，改为MD5加密后的秘闻，修改代码，前端提交的密码进行MD5加密后再根据数据库中密码比对
直接用DigestUtils.md5DigestAsHex(password.getBytes())内置函数就可以了

-----------------------------------------------------------
接口设计相关
接口管理平台用的APIfox，接口文档也有，在资源文件里，后续可以也po到github上

Swagger测试后端开发完之后用来测试，比postmanAPI等更高效，因为如果参数过多，postman接口设计会很复杂，swagger可以生成接口文档并且进行后端接口测试
使用方式：只要按照规范定义接口和接口信息，就可以做到生成接口文档，以及在线接口调试
这里使用的是Knife4j是为JavaMVC框架集成Swagger生成Api文档的增强解决方案(WebMvcConfiguration里)
1.导入knife4j的maven坐标
2.在配置类中加入knife4j相关配置
3.设置静态资源映射，否则接口文档页面无法访问（请求不知道doc.html是静态页面，会认为它是一个叫doc的controller请求）
除此之外指定生成接口文档的扫描路径的时候不要写错，不然看不到相关的controller了
配置好之后可以直接[localhost:8080/doc.html](http://localhost:8080/doc.html)访问生成的接口，并且进行测试等相关操作

实际上直接用apifox测试就行（根本不用这么麻烦）

Swagger的常用注解：可以控制生成的接口文档提高可读性
    @Api-用在类上，例如Controller，表示对类的说明
    @ApiModel-用在类上、例如entity、DTO、VO(直接以注解形式写在pojo的对应的vo和dto文件里了)
    @ApiModelProperty-用在属性上，描述属性信息
    @ApiOperation-用在方法上，例如Controller的方法，说明方法的用途、作用
-----------------------------------------------------------
通用开发流程
    1.需求分析和设计：对着产品原型分析需求，包括数据类型，校验，唯一性等等；接口设计；数据库设计
    2.代码开发:前后端逻辑：controller负责拿到前端传过来的json和各种请求数据 -> 调用对应的service中声明的方法 -> service中集中声明的各种方法在impl实现类中具体实现->实现类的方法实现来调用mapper和数据库交互
    impl这里需要注意，传进来的是DTO是为了方便封装前面提交过来的数据（即将前端的数据等封装成一个java实体）；但是为了最终传给mapper，建议用实体类，这里要做对象转换，把DTO转化为实体。如果DTO中有的属性和实体类都有而且属性名一致，可以直接用BeanUtils.copyProperties对象属性拷贝，不用一个一个属性set；至于DTO没有的但是实体有的就需要自行处理了（简单初始化或者像菜品分页查询的categoryName一样涉及到更复杂的多表联查）
    ->然后就可以实现mapper的数据库交互的语句了，简单的话就注解，复杂的话就用xml
    3.功能测试
        通过接口文档swagger测试(常用)
        通过前后端联调测试（前后端必须全开发好之后才能，所以一般不用）
    4.代码完善
        找到当前程序的问题，例如新增员工的已存在异常处理，或者临时写死的创建人id等这类问题

-----------------------------------------------------------

具体问题：

新增员工
1.新增员工的已存在员工500异常处理，即sql出现unique属性重复异常，在GlobalExceptionHandler里面捕获SQL异常
2.新增员工的时候修改人和处理人不能写死，要动态获取当前登录用户的id。基于JWT令牌认证的流程：
    用户认证（前端） - 提交用户名&密码 - 认证通过（后端）
    生成JWT Token（后端） - 生成JWT Token返回给前端 - 本地保存JWT Token（前端）
    请求后端接口（前端） - 每次请求都在请求头中携带JWT Token - 拦截请求验证JWT Token（后端拦截器，即项目的interceptor）
        if 验证通过 - 执行业务逻辑，返回数据（后端） - 展示数据（前端）
        if 验证不通过 - 返回错误信息（后端） - 展示错误信息并返回登录页面（前端）
综上，思路如下，interceptor拦截token之后解析，校验通过的话把empId拿出来，然后将它传给service的save方法（新增员工）里面，但是要怎么把解析出来的empId传给service的save呢？
————threadlocal技术
threadlocal为每个线程提供单独一份存储空间，具有线程隔离的效果，只有在线程内才能获取到对应的值，线程外的不能访问
问题解决思路：
    1.拦截器验证客户端发起的每一次请求对应一个线程：在service，controller，拦截器分别记录当前线程，发现三个打印的线程一致，下一次+1一致，说明每次请求对应一个线程，threadlocal这种情况下可行，因为每一个线程有一个单独的存储空间，内部可以共享，这样就可以在拦截器存，然后在这个请求到了service之后拿到
    2.threadlocal常用方法
        public void set(T value)设置当前线程的线程局部变量的值
        public T get()返回当前线程所对应的线程局部变量的值
        public void remove()移除当前线程的线程局部变量
        把这些封装进common的context的base context里面以供使用

员工分页查询
1.基本实现思路：先用PageQuery的DTO封装请求的三个参数name，page，pageSize，接下来需要考虑返回的数据类型。一般返回result有泛型，分页查询结果返还的PageResult要包含total总记录数和records（List）当前页数数据的集合；然后返回给前端的时候将所有PageResult封装成Result<PageResult>，Result的泛型就是PageResult。这个思路其实是分页查询通用的， 写在了common里
service实现的时候动态计算limit语句的起始位置的时候用到了mybatis-plus封装好的pagehelper；而且由于mapper是动态sql，写在employeemapper.xml里面，不注解
这个PageHelper.startPage本质上实现，就是pagehelper封装了mybatis拦截器，将分页参数存到了threadlocal里，所以后面的pageQuery方法不需要从startPage接受任何参数就能知道要分页并且计算出来页码和记录数。pagehelper源码在到了LOCAL_PAGE.set(page);之后把内容存到存储空间，分页查询前，又取出来了，然后会动态的把limit拼进去（xml里面调用mapper的时候的语句），其实页码和每页记录数也能动态算出来补全sql
2.后端给前端的create和updateTime是一个
"updateTime": 
[
2025,
2,
4,
23,
34,
29
],
需要调整成年月日时分秒形式
    1.在属性上加入注解，对日期进行格式化（只对当前属性有效）
    2.在server的config中的WebMvcConfiguration中扩展SpringMVC的消息转换器，统一对日期类型进行格式化处理

启用禁用员工账号和编辑员工还是一样的基本的和数据库的交互，无非两点注意，首先就是启用禁用员工实际上是修改status信息，这里就在mapper层做成了动态sql，更灵活，编辑信息就可以直接沿用了，然后就是编辑信息是两个接口一起实现的，一个是getById，一个是前面已经实现过的update

分类管理后续用于菜品套餐分类，也是很基础的功能模块，类似员工管理，就是换一张表

公共字段自动填充——技术开发而非业务开发，在一些业务表中的相同类似字段，比如说create_time,create_user，update_time,update_user等等，会出现大量类似相同代码，怎么解决：
    1.问题分析：代码冗余不便于后期维护
    2.实现思路：
        明确字段的操作类型，创建-insert；修改-insert、update
        使用切面统一处理，即在mapper层统一拦截需要处理的公共字段
        1 可以通过在mapper自定义注解（可以叫做AutoFill）用于标识需要进行公共字段自动填充的方法
        2 自定义切面类AutoFillAspect，统一拦截加入了AutoFill注解的方法，通过反射为公共字段赋值
        3 在mapper的方法上加入AutoFill注解
    3.代码开发
    4.功能测试
相关代码见AutoFillAspect，AutoFill（可以理解成注解AutoFill是一个标记，在这种情况下负责给方法打标记，切面类AutoFillAspect看作是统一拦截被标记注解的方法之后进行具体的操作(切面类由通知+切入点实现)）

新增菜品：根据类型查询分类 + 文件上传（阿里云oss） + 新增菜品提交表单数据
1.数据结构设计的时候注意菜品dish数据库的设计，其中的category_id是逻辑外键，不是物理外键，意思就是说在数据库里面并没有真的把这个外键关系创建出来，而是在程序内部维护这个字段，数据库层面并不认为是外键，但是程序中通过java代码处理可以当成外键用。逻辑外键的好处是比数据库外键效率更高，且维护的时候更方便。口味表dish_flavor的dish_id也是逻辑外键。
2.文件上传接口设计：浏览器（文件上传请求）->后端服务（拿到图片后将它上传到阿里云OSS对象存储）
阿里云oss：对象存储服务（开通OSS服务，创建存储空间bucket，获取并配置accessKey，参考sdk写入门，案例集成）
项目开发中第三方服务的通用使用思路：
    1.准备工作：注册云服务账号，认证，进入后台
    2.入门程序：参照官方SDK（software development kit）编写
    3.集成使用(配置在yml，工具类在AliOssUtil，然后配置类OssConfiguration里面把AliOssUtil配置出来)
阿里OSS实现文件上传的大致逻辑：
    // 初始AliOssUtil对象，通过注解自动注入
    // AliOssUtill的完整初始化流程：
    // 1. 在AliOssProperties.java提供配置属性类（配置属性类，用于读取配置文件中对应的的配置项然后封装成java对象）
    // 2. 在yml文件中配置相关属性
    // 3. 在OssConfiguration.java中创建AliOssUtil对象（@ConditionalOnMissingBean全局唯一即可）
    // 4. 在CommonController.java中使用@Autowired注解自动注入AliOssUtil对象
    // 这样的话就可以直接使用AliOssUtil对象和相关方法了
这里要注意一点，中间有段时间debug发现OssConfiguration启动项目拿不到四个属性，都是null，后来发现是因为yml文件的格式问题，最后alioss部分少了个缩进，要注意
需要注意的点：新增菜品涉及到口味，两张表同步处理需要@Transactional进行注解形式的事务管理保证原子性，要么都成功要么都失败
还有需要注意的点：主键回显：新增菜品的impl的部分口味表查数据需要菜品id，这里的菜品id是通过insert的“<insert id="insert" useGenerateKeys="true" keyProperty="id">”主键回显技术实现的
这里遇到了一个巨他妈的逆天的bug，编译不了，查了半个小时发现sql的insert语句主键回显的时候关键词useGeneratedKeys，copilot自动补全成了useGenerateKeys（无语）
还遇到了一个细节问题，就是debug的时候发现dish的impl拿到的DTO是null，是因为controller传参的时候@RequestBody的导入包导错了，要用Springboot Framework的

菜品分页查询：分页查询，根据页码展示信息数据，分页查询时根据需要输入名称、分类、状态查询（大同小异不做赘述）
需要注意的是分页返回数据里面的records的每条能通过菜品表拿到几乎能用到的所有数据，但是不能拿到categoryName（菜品表只存了id），所以需要多表联查找到categoryName。这就需要除了针对pagequery要设计一个dto之外（菜品基本属性），还需要设计一个vo（包含分类名称）。然后分页还是基于pagehelper插件，底层threadlocal那个，而且涉及到了针对categoryName的多表联产注意看一下xml，这块sql很复杂很考验功底
这块最后debug发现了一个小问题，select的标签里面忘了设置id和resultType了，下不为例
还有一个很蠢的问题，mybatis的if语句的标签是test不是text

删除菜品：一次删除一个或者批量；起售不能删；套餐关联菜品不能删；删除菜品，关联口味数据删
涉及到dish、dish_flavor、setmeal_dish三张表的操作，要理清楚，多看看相关的xml和mapper的sql怎么写的怎么想的
@RequestParam注解可以mvc内置动态解析字符串提取参数封装进入List对象
关键优化：dishServiceimpl中针对菜品删除部分的deleteBatch的最后一步，即for遍历菜品id后执行菜品和口味的两个sql，遍历量过大会导致性能过低。优化策略：sql语句减少，即批量删除而不是单独删除每个

修改菜品：业务相较新增菜品基本类似，但是增加了很多的回显内容，包括关联数据的回显（菜品和口味）；接口：查询分类（已实现）、图片文件上传（已实现）、根据id查询菜品（以及口味）、修改菜品。修改接口跟上传基本完全一致，除了这里id是必须的。

套餐新增、分页查询、删除、更新、起售停售略

-----------------------------------------------------------

拦截器：在server里面的interceptor，进行jwt令牌校验的拦截器，详见代码
由于频繁使用swagger进行后端测试，而jwt令牌会校验失败，因此利用一些方法统一在文档提交一个jwt令牌，利用登录获取一个令牌token，然后将令牌复制到swagger的文档管理，全局参数设置添加一个token参数

-----------------------------------------------------------

