package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface shoppingCartService {


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void save(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清除购物车
     */
    void cleanShoppingCart();
}
