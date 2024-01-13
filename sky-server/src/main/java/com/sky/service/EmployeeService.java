package com.sky.service;

import com.github.pagehelper.PageInfo;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.result.Result;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 根据前端传来的数据新增员工
     * @param employeeDTO
     * @return
     */
    Result<String> save(EmployeeDTO employeeDTO);

    /**
     * 根据前端传来的数据分页查询
     * @param epq
     * @return
     */
    PageResult page(EmployeePageQueryDTO epq);
}
