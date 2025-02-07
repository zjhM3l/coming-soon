package com.sky.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.PasswordConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.result.PageResult;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传入的密码进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    // 具体实现，调用持久层mapper，与数据库交互(insert)
    // 传入的是DTO，是为了方便封装前端提交过来的数据，但是和mapper交互时需要转换成实体类
    public void save(EmployeeDTO employeeDTO) {
        // 1、将DTO转换为实体类
        Employee employee = new Employee();
        // 注意这里不需要一个一个设置，因为DTO有的属性和实体类的属性名一致，所以可以直接使用BeanUtils.copyProperties进行属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);

        // 2、处理DTO中有但是实体类没有的属性
        employee.setStatus(StatusConstant.ENABLE); // 常量软编码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes())); // 默认密码
        // AutoFillAspect中已经实现了公共字段的自动填充，所以这里不需要手动设置
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        // 3、创建人和修改人为当前登录用户的ID，比较特殊单独拎出来
        // jwt登录流程中，登陆成功后生成token的时候ID存入了jwt的payload中，拦截器中解析token的时候将ID存入了ThreadLocal中
        // 因此可以直接从ThreadLocal中获取当前登录用户的ID
        // 因为每个请求都属于一个线程，所以可以使用ThreadLocal这一同线程的共享存储空间来传递
        // sky-common\src\main\java\com\sky\context\BaseContext.java就是封装好的threadlocal的工具类
        // 存ThreadLocal在\interceptor\JwtTokenAdminInterceptor.java实现，这里负责取

        // 调用工具类获取当前登录用户的ID
        // employee.setCreateUser(BaseContext.getCurrentId());
        // employee.setUpdateUser(BaseContext.getCurrentId());

        // 4、调用mapper方法，持久层插入（记得文件开始的时候Autowired注入）
        employeeMapper.insert(employee);
    }

    /** 
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // select * from employee limit 0,10
        // 底层基于mysql的limit分页，DTO已经封装了page和pageSize，可以动态记录limit的参数拼装
        // PageHelper可以简化分页代码编写，只需要在查询前调用startPage方法即可

        // 1、开启分页
        // 这里的pageHelper底层基于mybatis的拦截器实现，会把后一条sql动态拼接，动态的把limit拼进去计算
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 2、调用mapper方法
        // PageHelper要求返回结果为Page类型
        // 提问：为什么这里没有从上面拿到任何参数但是却知道怎么处理出来limit的参数？
        // 答：源码pageHelper也是基于threadlocal实现的，能拿到页码和每页记录数，然后动态拼接limit和计算参数
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        
        // 3、封装PageResult对象
        long total = page.getTotal();
        List<Employee> records = page.getResult();

        // 最后有个小问题，时间相关格式前端你啊到的是localdatetime类型，前端需要的是字符串格式的时间，可以再Employee类中使用@JsonFormat注解进行格式化
        // 建议的解决方法是在WebMvcConfiguration中扩展SpringMVC的消息转换器，统一对日期类型进行格式化处理
        return new PageResult(total, records);
    }

    /**
     * 启用或禁用员工
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?
        // 为了提高通用性，在mapper不实现只针对status的更新，而是实现针对任意字段的更新
        // 由于mapper.xml中使用了动态sql，所以需要传入一个实体类对象

        // 1、创建实体类对象
        // Employee employee = new Employee();

        // 2、设置属性
        // employee.setStatus(status);
        // employee.setId(id);

        // 也可以使用Builder模式
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        // 3、调用mapper方法
        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    public Employee getById(Long id) {
        // select * from employee where id = ?
        Employee employee = employeeMapper.getById(id);
        // 密码不让看
        employee.setPassword("****");
        return employee;
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    public void update(EmployeeDTO employeeDTO) {
        // 1、将DTO转换为实体类
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // 2、设置修改时间和修改人
        // AutoFillAspect中已经实现了公共字段的自动填充，所以这里不需要手动设置
        // employee.setUpdateTime(LocalDateTime.now());
        // employee.setUpdateUser(BaseContext.getCurrentId());

        // 3、调用mapper方法
        employeeMapper.update(employee);
    }
}
