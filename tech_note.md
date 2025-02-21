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

拦截器：在server里面的interceptor，进行jwt令牌校验的拦截器，详见代码
由于频繁使用swagger进行后端测试，而jwt令牌会校验失败，因此利用一些方法统一在文档提交一个jwt令牌，利用登录获取一个令牌token，然后将令牌复制到swagger的文档管理，全局参数设置添加一个token参数

-----------------------------------------------------------

管理端具体问题：

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

套餐新增、分页查询、删除、更新、起售停售略。文档在day4

店铺营业设置：Redis
1.Redis入门：
    简介：Redis是一个基于内存的key-value结构数据库
    MySQL和Redis的区别：
        介质：MySQL是基于硬盘的，Redis是基于内存的，读写性能高，适合存储热点数据（在某个时间段内访问频率高的数据）
        数据结构：MySQL是基于二维表的，Redis是基于key-value的
    win项目用redis服务：
        1. 启用：安装目录cmd，输入：redis-server.exe redis.windows.conf
        2. 连接
            2.a 本地redis：安装目录cmd，输入：redis-cli.exe
            2.b 远程redis：安装目录cmd，输入：redis-cli.exe -h 【ip】 -p 【port】例如 redis-cli.exe -h localhost -p 6379指定地址和端口
        3. 退出：exit
        4. 设置密码：在redis.windows.conf中设置requirepass 【password】例如 requirepass 123456，记得去掉注释符号。Redis没有用户概念，只有密码概念，有密码就行
    Win的Redis图形界面：Redis-Desktop-Manager(要在上述命令执行后才能连接)
2.Redis常用数据类型：Redis存储的是key-value结构的数据，key是字符串类型，value可以是以下类型，注意下列是value的结构：
    字符串string
    哈希hash：field-value，类似HashMap结构；适合存储对象，例如：{"name":"张三","age":18}，name和age是field，张三和18是value
    列表list：类似队列，按照插入顺序排序，可以有重复元素，类似Java中的LinkedList；适合存储和顺序有关的数据
    集合set：无序集合，无重复元素，类似HashSet；适合存储不重复的数据
    有序集合zset/sorted set：集合中每个元素关联一个分数score，根据分数升序排序，没有重复元素；适合存储排行榜数据
3.Redis常用命令
    Redis和MySQL的区别：MySQL的命令基于sql，不需要考虑数据类型，Redis的命令基于数据类型，需要考虑数据类型
    字符串操作命令
        SET key value：设置key的值为value
        GET key：获取key的值
        SETEX key seconds value：设置key的值为value，seconds秒后过期；常见于短信验证码
        SETNX key value：只有在key不存在时设置key的值为value；常见于分布式锁
    哈希hash操作命令
        Redis hash是一个string类型的field和value的映射表，也就是整个数据结构类似key-value结构，其中value由多个field-value对组成
        HSET key field value：将哈希表key的field的值设为value
        HGET key field：获取key的field的值
        HDEL key field：删除key的field
        HKEYS key：获取key的所有field
        HVALS key：获取key的所有value
    列表操作命令
        Redis list是简单的字符串列表，按照插入顺序排序，即key-value，其中value是一个列表且每个元素都是字符串
        LPUSH key value1 [value2]：将一个或多个值value插入到列表key的表头
        LRANGE key start stop：获取列表指定区间内的元素，
        RPOP key：移除并返回列表的最后一个元素
        LLEN key：获取列表的长度
    集合操作命令
        Redis set是string类型的无序集合，集合成员唯一且无序 
        SAAD key member1 [member2]：向集合添加一个或多个成员
        SMEMBERS key：返回集合中的所有成员
        SCARD key：返回集合的成员数
        SINTER key1 [key2]：返回给定所有集合的交集
        SUNION key1 [key2]：返回给定所有集合的并集
        SREM key member1 [member2]：移除集合中一个或多个成员
    有序集合zset操作命令
        Redis zset是string类型的有序集合，集合成员唯一且每个元素关联一个double类型的分数score
        ZADD key score1 member1 [score2 member2]：向有序集合添加一个或多个成员，或者更新已存在成员的分数
        ZRANGE key start stop [WITHSCORES]：返回有序集中指定区间内的成员，如果指定了WITHSCORES选项，则一并返回成员的排名和分数
        ZINCRBY key increment member：为有序集合中的成员的分数加上增量increment
        ZREM key member1 [member2]：移除有序集合中的一个或多个成员
    通用命令
        KEYS pattern：查找所有符合给定模式pattern的key
        EXISTS key：检查给定key是否存在
        TYPE key：返回key所储存的值value的类型
        DEL key：该命令用于在key存在时删除key
4.在Java中操作Redis
    Redis的Java客户端有很多，常用的有Jedis、Lettuce、Spring Data Redis
    Spring Data Redis框架操作Redis
        1. 导入Spring Data Redis的maven坐标
        2. 配置Redis数据源
        3. 编写配置类RedisConfiguration，创建RedisTemplate对象
        4. 通过RedisTemplate对象操作Redis
        Spring Data Redis提供的针对五类数据类型的接口
            1. ValueOperations valueOperations = redisTemplate.opsForValue();
            2. ListOperations listOperations = redisTemplate.opsForList();
            3. SetOperations setOperations = redisTemplate.opsForSet();
            4. HashOperations hashOperations = redisTemplate.opsForHash();
            5. ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        这里要注意的是，redis的string和java的string是有区别的，redisTemplate可以接收任意类型的数据，在config时指定了序列化规则为string后，可以自动将任意类型数据序列化为string类型
        务必认真看SpringDataRedisTest测试类
店铺营业设置开发:修改和查询的接口；查询营业状态在商家/用户都要查，所以二者最好用分开的查询接口，遵从约定管理端从/admin开始，用户端从/user开始
其中用户和管理端的查询状态接口不需要请求参数，返还还是老两样code、msg和表示状态的int data，这样的话没必要存到表里（只有一个字段而且只有一列0或者1），不如存到redis里面。
综上分析过程：营业状态数据存储方式最好基于Redis字符串进行存储 SHOP_STATUS-value（0或1）

订单状态定时处理（SpringTask）
    SpringTask:
        1.介绍：Spring框架提供的任务调度工具，可以按照约定的时间自动执行某个代码逻辑；定位为定时任务框架，定时自动执行某段Java代码
        2.cron表达式：本质上是一个字符串，可以定义任务触发的时间
            -构成规则：表达式分为6或7个空格分隔的域，每个域从左到右分别代表
                -秒（0-59）
                -分钟（0-59）
                -小时（0-23）
                -日期（1-31）
                -月份（1-12）
                -星期（1-7，1表示星期天）
                -年（1970-2099，可选）
            -构成示例：0 0 9 12 10 ? 2022
                -表示2022年10月12日9点0分0秒执行
                -日和星期二选一，一般不同时使用
            特殊字符：
                -*：表示所有值
                -?：表示不指定值
                --：表示范围
                -,：表示列举值
                -/：表示增量
                -L：表示最后一天
                -W：表示离某天最近的工作日
                -#：表示第几个星期几
            一般用在线的cron表达式生成器生成
        3.SpringTask使用
            -导入maven坐标spring-context（没有自己的包都集成在这里）
            -启动类添加注解@EnableScheduling开启任务调度
            -自定义定时任务类（重要MyTask）
    订单状态定时处理：
        -下单后未支付，一直处于待支付；用户收货后管理端未点击完成，一直派送中
        -实现思路：springtask，对于情况一每分钟检查一次是否存在超时订单（下单超过15min未支付），如果存在修改状态为已取消 ；第二种情况派送中订单一般一天检查一次，每天凌晨1点检查一次是否存在派送中，存在则修改为已完成

WebSocket：基于TCP的一种新的网络协议，实现了浏览器与服务器全双工通信——浏览器和服务器只需要完成一次握手，两者既可以创建持久性链接，并进行双向数据传输
    HTTP：请求/响应模型，每一次浏览器向服务器发送请求，服务器处理完请求后返回响应，且连接失效（短连接，下一次就是新的连接）。必须客户端先发起请求，服务器才能响应，不能颠倒
    WebSocket：
        1. 客户端向服务器发送handshake请求
        2. 服务器响应handshake请求，返还回一个acknowledgement
        3. 客户端和服务器建立持久性双向连接（长连接）
        4. 什么时候不需要了，客户端或服务器都可以关闭连接
    HTTP和WebSocket的区别：
        1. HTTP是短连接，WebSocket是长连接
        2. HTTP是单向请求/响应模型，WebSocket是双向通信
    相同点：都是基于TCP协议的
    常见应用场景：
        1. 视频弹幕（WebSocket主动把信息推送到浏览器）
        2. 网页聊天室（通过服务器把消息主动推送到网页）
        3. 实时更新股票/赛事数据（主动把消息推送到网页）
    Java开发WebSocket实现步骤案例：
        1. 页面websocket.html作为客户端
        2. 导入WebSocket的maven坐标
        3. 导入服务端组件WebSocketServer负责和客户端通信
        4. 导入配置类WebSocketConfiguration注册WebSocket的服务端组件（配置类代码固定，哪个任务用到websocket都是这个）
        5. 导入定时任务类WebSocketTask定时向客户端推送数据

来单提醒（Websocket）
    需求：用户下单并且支付后，第一时间通知外卖商家（语音播报+弹出提示框）
    设计：
        1. 通过WebSocket实现管理端页面和服务端保持长连接状态（WebSocketServer）
        2. 客户支付后，调用WebSocket的相关API实现服务端向客户端推送消息(paySuccess)
        3. 客户端浏览器解析服务端推送的消息，判断是来单提醒还是客户催单，进行相应的消息提示和语音播报
        4. 约定服务端发送给客户端浏览器的数据格式为JSON，字段包括：type,orderId,content

客户催单（Websocket）
    需求：支付成功且待接单，用户可以在小程序催单，需要第一时间通知外卖商家
    设计：
        1. 通过WebSocket实现管理端页面和服务端保持长连接状态（WebSocketServer）
        2. 客户点击催单后（user的ordercontroller里面调），调用WebSocket相关API实现服务端向客户端推送消息
        3. 客户端浏览器解析服务端推送的消息，判断是来单提醒还是客户催单，进行相应的消息提示和语音播报
        4. 约定服务端发送给客户端浏览器的数据格式为JSON，字段包括：type,orderId,content

数据统计-业务报表（Apache Echarts）
Apache Echarts
    介绍：基于 JavaScript 的开源数据可视化图表库，提供了丰富的图表类型和交互功能，适用于数据可视化场景。
    使用：
        1. 在前端项目中引入 Echarts 库（echarts.js文件）
        2. 使用 JavaScript 创建图表实例（官网有模版），并配置数据和样式（官网都有很详细的使用说明）
        3. 重点是需要后端提供符合格式要求的动态数据，然后响应给前端来显示图表

数据导出表Excel（Apache POI）
处理microsoft office各种文件格式，一般操作excel

-----------------------------------------------------------

swagger用户端和商家接口文档的区分，修改WebMvcConfiguration配置类
复制一个docket，区分扫描用户和admin的包

-----------------------------------------------------------

用户端具体问题：

微信登录：
使用HttpClient（在java程序中构造请求并发送）请求微信某接口，实现微信登录
HttpClient：是Apache Jakarta Common下的子项目，可以用来提供高效的、最新的、功能丰富的支持HTTP协议的客户端编程工具包，并且它支持HTTP协议最新的版本和建议
即允许Java通过编码的方式来发送HTTP请求
使用：详见HttpClientTest.java的请求使用测试示例
    1. 导入Maven依赖（导入的aliyun-oss-sdk底层是httpclient，所以不用再单独导入了）
    2. 核心API：
        HttpClient：接口，发送http请求
        HttpClients：构建器，可以创建HttpClient对象
        CloseableHttpClient：具体实现类，实现了HttpClient接口
        HttpGet：发送GET请求
        HttpPost：发送POST请求
    3. 发送请求步骤
        创建HttpClient对象
        创建Http请求对象，结合请求类型构建HttpGet或HttpPost对象
        调用HttpClient的execute方法发送请求
相关工具类已经封装进入HttpClientUtil.java

微信小程序开发流程
    1. 准备工作
        -注册小程序
        -完善小程序信息
        -下载开发者工具
    2. 开发小程序（本质是用js开发前端）
        目录结构：主体部分三个文件，必须放在根目录
            -app.js：小程序逻辑
            -app.json：小程序公共配置
            -app.wxss：可有可无，小程序公共样式表（类似css）
        页面：小程序每个页面放在pages下面，其中每个页面包含四个文件
            -js：页面逻辑
            -wxml：页面结构
            -json：页面配置（可选）
            -wxss：页面样式表（可选）
    3. 提交审核和发布（上传代码然后去管理页面送审）

微信登录具体实现：https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html
主要实现流程在UserController-UserService-impl-mapper要注意的是，小程序登录之后的token是authentication，这个需要校验，需要一个拦截器校验小程序端的请求JwtTokenUserInterceptor.java，写完拦截器别忘了去WebMvcConfiguration注册一下，这里格外注意line55要跳过status，因为在登录前进入页面之后用户就需要看到status,这个请求发起要早于登录

商品浏览，略

缓存商品-Redis
-缓存菜品
    问题说明：短时大量访问，菜品数据查询导致数据库性能下降
    实现思路：通过Redis缓存菜品数据，减少数据库查询操作
        查询菜品-后端服务-查询缓存-有缓存读取、无缓存从数据库读取载入缓存
        redis数据结构设计：
            缓存逻辑分析：粒度按照分类展示菜品，每个分类下的菜品为一份缓存数据，key（dish_分类的ID）-value（String菜品名称的List集合序列化转成Redis字符串）
        数据库中菜品数据有变更时清理缓存数据（确保一致性），包含了，修改、新增、删除、起售停售之后都需要清理缓存。这块还可以用AOP，写一个缓存切面拦截清除缓存的指令;或者抽取出来单独方法cleanCache
        还有一点要注意的是，redis相关操作在controller层操作
-缓存套餐（进一步利用Spring Cache简化Redis编码提高开发效率）
    Spring Cache简化代码（提供注解简化代码）
        SpringCache是一个框架，实现了基于注解的缓存功能，只需要简单的加一个注解，就能实现缓存功能
        SpringCache提供了一层抽象，底层可以切换不同的缓存实现如EHCache、Caffeine、Redis（pom导入任何缓存坐标，springcache可以自动检测底层用什么缓存）
        常用注解
            @EnableCaching：开启缓存注解功能，加在启动类上
            @Cacheable：在方法执行前先查询缓存中是否有数据，如果有数据，则直接返回缓存数据；如果没有缓存数据，调用方法并将方法返回值放到缓存中
            @CachePut：将方法的返回值放到缓存中
            @CacheEvict：将一条或多条数据从缓存中删除
    实现思路
        1. 导入Spring Cache和Redis相关maven坐标
        2. 在启动类上加入@EnableCaching注解，开启缓存注解功能
        3. 在用户端接口SetmealController的list方法加上Cacheable注解
        4. 在管理端口SetmealController的save、delete、update、startOrStop等方法上加入CacheEvict注解

购物车:添加、删减、查看、清空，注意这里在设计cart表时适当的引入了冗余字段，但是冗余字段要求不能经常变化而且不建议经常使用，属于空间换时间，98/199；11:00处；而且注意简单分析一下就知道购物车不能缓存;impl的addShoppingCart的逻辑比较复杂，需要考虑的情况比较多，注意一下

用户下单：
地址簿单表增删改查，一个用户多个地址但是只有一个默认地址，订单这里的手机号和详细地址也是冗余字段，订单详情的name和image也是，这里记得去看数据库设计文档。还有就是多表操作（订单、订单详情）事务注解，原子化一下；还有就是订单插入需要主键回填，订单明细创建要用。主键回填用过很多次了，不过多赘述。
    1.商品进入购物车
    2.订单提交
    3.订单支付（此时订单数据已经产生，大师没有支付）
    4.付款成功

订单支付：代码基本上统一的现成的，稍微改造，主要了解流程，直到怎么用就行，因为这里没有企业资质没法真正实现
微信支付流程：
    1.微信支付介绍
        微信支付产品：扫码、JSAPI（H5应用）、小程序、Native（商家二维码）、APP、刷脸
        微信支付流程：
            小程序支付：时序图在文档可以看
            1. 小程序端发起下单请求到商户系统，商户系统返回订单号
            2. 小程序端发起申请微信支付请求到商户系统
            3. 商户系统发起调用微信下单接口（预下单接口生成预支付交易单）到微信后台，微信后台返回预支付交易标识
            4. 商户系统将组合数据再次签名（防止被截获）并返回支付参数给小程序端
            5. 用户确认支付，小程序端直接向微信后台调起微信支付（真正完成付款）请求，微信后台返回支付结果，小程序端显示支付结果
            6. 同时温馨后台返回支付结果推送给商户系统，商户系统更新订单状态
        重要接口：
            1. 步骤3中商户系统调用后台的预下单接口：JSAPI下单
            2. 步骤5中小程序端调用后台的调起支付接口：小程序支付wx.reqeustPayment（真正实现付款）
            3. 步骤5和6的回调接口：支付结果通知
    2.微信支付准备工作
        1. 步骤3已经后续多次商户系统需要调用微信后台接口以及接受数据，确保安全性需要加密、解密、签名，这里需要处理
            平台证书文件+商户私钥文件.pem
        2. 步骤6微信后台推送支付结果到商户系统本质上是HTTP请求，需要一个公网IP而不是局域网IP，这里需要内网穿透
            获取临时域名：就是一个公网IP，微信后台可以通过这个IP回调程序；使用cpolar进行内网穿透（操作教程见119/199后半段）
                临时ip：http://6e578268.r3.cpolar.cn
    3.代码导入（可以直接使用代码，很固定可复用，替换企业码即可）
        具体代码的每个部分每一行的解释和理解都在对应文件的注释里面，自己下去看（建议结合微信小程序官方文档的支付时序图看
        application.yml配置
        OrderController的订单支付
        OrderService的payment和paySuccess
        OrderServiceImpl的对应的实现
        OrderMapper的getByNumber和update，update还有对应mapper.xml的
        UserMapper的getById也是
        notify里面的PayNotifyController，负责支付成功的回调

-----------------------------------------------------------

-----------------------------------------------------------

