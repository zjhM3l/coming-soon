package com.sky.service.impl;

import java.time.LocalDateTime;

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

import com.sky.constant.PasswordConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;

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
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 3、创建人和修改人为当前登录用户的ID，比较特殊单独拎出来
        // jwt登录流程中，登陆成功后生成token的时候ID存入了jwt的payload中，拦截器中解析token的时候将ID存入了ThreadLocal中
        // 因此可以直接从ThreadLocal中获取当前登录用户的ID
        // 因为每个请求都属于一个线程，所以可以使用ThreadLocal这一同线程的共享存储空间来传递
        // sky-common\src\main\java\com\sky\context\BaseContext.java就是封装好的threadlocal的工具类
        // 存ThreadLocal在\interceptor\JwtTokenAdminInterceptor.java实现，这里负责取

        // 调用工具类获取当前登录用户的ID
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        // 4、调用mapper方法，持久层插入（记得文件开始的时候Autowired注入）
        employeeMapper.insert(employee);
    }

}
