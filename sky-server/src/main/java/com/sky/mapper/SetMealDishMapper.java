package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedList;
import java.util.List;

@Mapper
public interface SetMealDishMapper {
    /**
     * 根据菜品id值查询套餐的id
     * @param ids
     * @return
     */
    List<Long> getSmByIds(List<Long> ids);

    /**
     * 添加套餐菜品关系表
     * @param setmealdishlist
     */
    void addSetMealDish(List<SetmealDish> setmealdishlist);

    /**
     * 通过套餐id获取关联的dish
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getDishById(Long id);

    /**
     * 根据套餐id删除套餐和菜品的关联关系
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);


    void deleteBatch(List<Long> ids);

    /**
     * 根据套餐id查询其对应的菜品的statusList
     * @param id
     */
    List<Integer> getDishStatusById(Long id);
}
