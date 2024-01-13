package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.beans.beancontext.BeanContext;
import java.time.LocalDateTime;

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
        //对密码前端传来的加密处理
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
     * 实现新增员工
     *
     * @param employeeDTO
     * @return
     */
    @Override
    public Result<String> save(EmployeeDTO employeeDTO) {
        Employee emp = new Employee();

        //将dto的数据copy到实体类中
        BeanUtils.copyProperties(employeeDTO, emp);

        //设置employee中的其他数据
        emp.setStatus(StatusConstant.ENABLE);
        emp.setCreateTime(LocalDateTime.now());
        emp.setUpdateTime(LocalDateTime.now());
        emp.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        emp.setCreateUser(BaseContext.getCurrentId());
        emp.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.save(emp);
        return Result.success();
    }

    /**
     * 分页查询
     * @param epq
     * @return
     */
    @Override
    public PageResult page(EmployeePageQueryDTO epq) {
        PageHelper.startPage(epq.getPage(),epq.getPageSize());

        Page<Employee> page = employeeMapper.page(epq);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 启用/禁用员工
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, long id) {
//        Employee emp = new Employee();
//        emp.setStatus(status);
//        emp.setId(id);

        Employee emp = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(emp);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee getById(long id) {
        Employee emp = employeeMapper.getById(id);
        //密码设为*加强安全性
        emp.setPassword("****");
        return emp;
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setUpdateTime(LocalDateTime.now());

        employeeMapper.update(employee);

    }
}
