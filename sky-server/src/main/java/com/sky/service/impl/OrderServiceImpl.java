package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.shoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.vo.OrderVO_old;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private shoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理异常信息 地址为空/购物车为空

        //地址为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //获取用户id
        Long userid = BaseContext.getCurrentId();

        //购物车为空
        ShoppingCart cart = ShoppingCart.builder().userId(userid).build();
        List<ShoppingCart> list = shoppingCartMapper.list(cart);
        if (list == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入一条数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));//订单号 以时间戳代替
        order.setUserId(userid);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());

        orderMapper.insert(order);

        List<OrderDetail> arrayList = new ArrayList();
        //向订单明细表插入若干条数据
        for (ShoppingCart sc : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(sc, orderDetail);
            orderDetail.setOrderId(order.getId());
            arrayList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(arrayList);

        //清空当前用户购物车数据
        shoppingCartMapper.deleteByUserId(userid);
        //封装返回vo对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 查询历史订单
     *
     * @param
     * @return
     */
    public PageResult page(int page, int pageSize, Integer status) {
        //设置分页
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        //查询status对应的order数据
        Page<Orders> orderspage = orderMapper.pageQuery(ordersPageQueryDTO);
        if (orderspage != null && orderspage.getTotal() > 0) {
            List<Orders> ordersList = orderspage.getResult();

            //查询orderdetail数据 并且将order和orderdetail封装到ordervo数组中
            List<OrderVO> list = new ArrayList();
            for (Orders orders : ordersList) {
                List<OrderDetail> detailList = orderDetailMapper.getByOrderDetail(orders.getId());
                OrderVO vo = new OrderVO();
                BeanUtils.copyProperties(orders, vo);
                vo.setOrderDetailList(detailList);
                list.add(vo);
            }
            return new PageResult(orderspage.getTotal(), list);
        }

        //若查无数据 返回null
        return null;
    }

    /**
     * 查询订单详细
     *
     * @param id
     * @return
     */
    public OrderVO checkOrderDetail(Long id) {
        //查询order
        Orders orders = Orders.builder().id(id).build();
        Orders order = orderMapper.getByOrder(orders);
        //查询order_detail
        List<OrderDetail> detailList = orderDetailMapper.getByOrderDetail(order.getId());

        //封装到vo并返回
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderDetailList(detailList);
        return vo;
    }

    /**
     * 未实现支付功能 待付款状态用户端会显示已取消？
     * 取消订单
     *
     * @param id
     */
    public void cancel(Long id) {
        //获取该用户的订单信息
        Orders orderOri = Orders.builder().id(id).build();
        Orders order = orderMapper.getByOrder(orderOri);

        // 校验订单是否存在
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //已接单或者派送中
        if (order.getStatus() == 3 || order.getStatus() == 4) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 更新订单状态、取消原因、取消时间 新建orders只保存要修改的数据 提高效率
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    public void getSame(Long id) {
        //根据订单id获得orderdetailList
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderDetail(id);
        //orderdetailList封装到购物车实体 并为userid，创建时间赋值
        if (orderDetailList != null && orderDetailList.size() > 0) {
            List<ShoppingCart> cartList = new ArrayList<>();
            for (OrderDetail order : orderDetailList) {
                ShoppingCart cart = new ShoppingCart();
                BeanUtils.copyProperties(order, cart);
                cart.setUserId(BaseContext.getCurrentId());
                cart.setCreateTime(LocalDateTime.now());
                cartList.add(cart);
            }
            //将购物车list加入到购物车表中
            shoppingCartMapper.insertBatch(cartList);
        }
    }

    /**
     * 管理端订单搜索
     * endtime可对应订单取消时间/到达时间 本项目为实现支付 所以以订单取消时间为准
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult pageForAdmin(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //根据条件查询订单数据
        Page<Orders> pageQuery = orderMapper.pageQuery(ordersPageQueryDTO);
        //创建vo对象 并进行封装
        List<OrderVO> voArrayList = new ArrayList<>();
        List<Orders> ordersList = pageQuery.getResult();
        //如果查询为空则返回报错信息
        if (ordersList == null || ordersList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //向voList字段赋值
        for (Orders orders : ordersList) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(orders, vo);
            String orderDishes = getOrderDishesStr(orders);
            vo.setOrderDishes(orderDishes);
            voArrayList.add(vo);
        }

        //封装到pageresult并返回
        return new PageResult(pageQuery.getTotal(), voArrayList);
    }

    /**
     * 根据订单获取对应菜品字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> detailList = orderDetailMapper.getByOrderDetail(orders.getId());

        StringBuilder str = new StringBuilder("");

        for (OrderDetail detail : detailList) {
            String sam = detail.getName() + "*" + detail.getNumber() + ";";
            str.append(sam);
        }
        return str.toString();
    }

    /**
     * 各个状态数量统计
     * @return
     */
    public OrderStatisticsVO statistics() {
        //派送
        Integer confirmed = orderMapper.staticsByStatus(4);
        //待派送
        Integer deliveryInProgress = orderMapper.staticsByStatus(3);
        //待接单
        Integer toBeConfirmed = orderMapper.staticsByStatus(2);

        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setConfirmed(confirmed);
        vo.setDeliveryInProgress(deliveryInProgress);
        vo.setToBeConfirmed(toBeConfirmed);
        return vo;
    }

    /**
     *查询订单详情
     * @param id
     * @return
     */
    public OrderVO details(Long id) {
        //获取订单表数据
        Orders orders = Orders.builder().id(id).build();
        Orders order = orderMapper.getByOrder(orders);

        if (order==null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        //获取订单详情表
        List<OrderDetail> detailList = orderDetailMapper.getByOrderDetail(id);
        //获取orderDishes
        String str = getOrderDishesStr(order);

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order,vo);
        vo.setOrderDishes(str);
        vo.setOrderDetailList(detailList);
        return vo;
    }

    /**
     * 接单
     * @param id
     */
    public void confirm(Long id) {
        Orders orders = Orders.builder().id(id).status(Orders.CONFIRMED).build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        //只有待接单可以拒单 拒单需要原因 并且需要退款
        Orders orders = Orders.builder().id(ordersRejectionDTO.getId()).build();
        Orders order = orderMapper.getByOrder(orders);

        if (order.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //设置退款数据
        order.setStatus(Orders.CANCELLED);
        order.setPayStatus(Orders.REFUND);
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        //更新数据
        orderMapper.update(order);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    public void cancelForAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders or = Orders.builder().id(ordersCancelDTO.getId()).build();
        Orders order = orderMapper.getByOrder(or);

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    public void delivery(Long id) {
        Orders or = Orders.builder().id(id).build();
        Orders ordersDB = orderMapper.getByOrder(or);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    public void complete(Long id) {
        Orders or = Orders.builder().id(id).build();
        Orders ordersDB = orderMapper.getByOrder(or);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }
}
