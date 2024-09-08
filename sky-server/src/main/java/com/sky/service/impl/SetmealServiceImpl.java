package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealOverViewVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;


    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        //创建setmeal对象
        Setmeal setmeal = new Setmeal();
        //对象属性拷贝
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //将套餐插入数据库
        setmealMapper.insert(setmeal);

        //将菜品加入套餐菜品表setmeal_dish
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //批量插入数据
        setMealDishMapper.insertDishes(setmealDishes);
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteSetmeal(List<Long> ids) {
        //批量删除套餐
        setmealMapper.deleteByIds(ids);

        //批量删除与套餐关联的setmeal_dish
        setMealDishMapper.deleteByDishIds(ids);

    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        //修改套餐数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        List<Long> ids = new ArrayList<>();
        ids.add(setmealDTO.getId());
        //删除原有的套餐菜品数据
        setMealDishMapper.deleteByDishIds(ids);

        //插入新的套餐菜品数据
        setMealDishMapper.insertDishes(setmealDTO.getSetmealDishes());
    }

    /**
     * 修改套餐状态
     * @param id
     * @param status
     */
    @Override
    public void setSetmealStatus(String id, String status) {
        setmealMapper.updateSetmealStatusById(id, status);
    }

    /**
     * 根据套餐id查询套餐，用于修改时的数据回显
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmeal(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        //查询套餐下的所有菜品
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setMealDishMapper.getBySetmealId(setmealId);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
