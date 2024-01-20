package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 向订单明细表插入若干数据
     * @param arrayList
     */
    void insertBatch(List<OrderDetail> arrayList);

    /**
     * 根据orderdetail部分信息动态查询对应订单详情表
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderDetail(Long orderId);
}
