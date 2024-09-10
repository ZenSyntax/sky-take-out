package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    @CacheEvict(cacheNames = "dishCache", key = "dishDTO.categoryId")//精确清理缓存数据，将该菜品所属分类下的菜品缓存清除
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveDishWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * 由于参数类型是params，所以不需要加注解即可直接使用对象接收
     * 如果不使用对象接收，就需要使用@RequestParam注解逐个接收
     * @param dishPageQueryDTO
     * @return
     */
    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 前端传来的数据格式为raw参数，是一个结构为id1, id2, id3, ..., idn的字符串类型数据
     * 故最好是封装为一个list集合，而@RequestParam注解（SpringMVC自动类型转换）可以将这种字符串直接转换为List<Long>
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("菜品批量删除：{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 用于修改页面的数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> selectById(@PathVariable Long id) {
        log.info("根据id查询菜品数据：{}", id);
        DishVO dishVO =dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result updateDishWithFlavor(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateDishWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(@RequestParam String categoryId) {
        log.info("查询分类{}下的所有菜品", categoryId);
        List<DishVO> dishVOS = dishService.getDishesByIdWithFlavor(categoryId);
        return Result.success(dishVOS);
    }

    /**
     * 修改菜品的起售停售状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品售卖状态")
    @CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result updateStatus(@PathVariable Integer status, @RequestParam Integer id) {
        log.info("修改菜品{}的出售状态为：{}", id, status == StatusConstant.ENABLE ? "起售中":"停售中");
        dishService.updateDishStatus(id, status);
        return Result.success();
    }
}
