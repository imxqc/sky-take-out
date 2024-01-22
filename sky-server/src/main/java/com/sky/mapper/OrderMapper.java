package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

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

    /**
     * 获取定时任务对应的订单
     * @param status
     * @param time
     * @return
     */
    List<Orders> getTaskOrder(Integer status, LocalDateTime time);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);


    /**
     * 订单id搜索订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 获取营业额
     * @param map
     * @return
     */
    Double getAmountTotal(HashMap map);

    /**
     * 根据map获取对应订单信息
     * @param map
     * @return
     */
    Integer countByMap(HashMap map);

    /**
     * 获取销量数据
     * @param beginTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);
}
