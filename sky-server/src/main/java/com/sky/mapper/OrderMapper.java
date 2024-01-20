package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper {
    /**
     * 插入一条订单表数据
     * @param order
     */
    void insert(Orders order);


    /**
     * 根据传来的order动态查询order表的数据
     * 该mapper暂时填写一部分 可后续根据需要添加查询条件
     * @param orders
     * @return
     */
    Orders getByOrder(Orders orders);

    /**
     * 根据dto分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 更新order表中数据
     * @param orders
     */
    void update(Orders orders);

    /**
     * 各个状态数量统计
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer staticsByStatus(Integer status);
}
