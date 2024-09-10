package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {
    /**
     * 根据菜品id查套餐id
     * @param ids
     * @return
     */
    List<Long> getSetMealDishIdsByDishIds(List<Long> ids);

    /**
     * 批量插入套餐菜品
     * @param setmealDishes
     */
    void insertDishes(List<SetmealDish> setmealDishes);

    /**
     * 批量删除套餐菜品
     * @param ids
     */
    void deleteByDishIds(List<Long> ids);

    /**
     * 根据setmealId查询套餐下的所有菜品
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
