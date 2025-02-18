package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Api(tags = "菜品相关接口")
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    private static final String DISH_KEY = "dishCache_";
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation("新增菜品接口")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO)
    {
        log.info("新增的菜品为: {}", dishDTO);
        dishService.save(dishDTO);
        cleanCache(DISH_KEY + dishDTO.getCategoryId());
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("page")
    @ApiOperation("菜品分页查询接口")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("要查询的菜品分页为: {}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品接口")
    public Result delete(@RequestParam List<Long> ids)   //这里需要加@RequestParam注解，才能实现String到List<Long>的自动转换
    {
        log.info("批量删除菜品: {}", ids);
        dishService.delete(ids);
        cleanCache(DISH_KEY + "*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> selectById(@PathVariable Long id)
    {
        log.info("根据id: {} 查询菜品", id);
        DishVO dishVO = dishService.selectByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品接口")
    public Result update(@RequestBody DishDTO dishDTO)
    {
        log.info("修改菜品: {}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        cleanCache(DISH_KEY + "*");
        return Result.success();
    }

    /**
     * 修改菜品状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品状态接口")
    public Result startOrStop(@PathVariable Integer status, Long id)
    {
        log.info("起售/停售菜品: {}", id);
        dishService.startOrStop(status, id);
        cleanCache(DISH_KEY + "*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> selectByCategoryId(Long categoryId)
    {
        log.info("根据分类id查询菜品: {}", categoryId);
        List<Dish> dishes = dishService.selectByCategoryId(categoryId);
        return Result.success(dishes);
    }

    /**
     * 根据pattern删除对应的键值对
     * @param pattern
     */
    private void cleanCache(String pattern)
    {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

}
