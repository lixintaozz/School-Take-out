package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.entity.SetmealDish;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Api(tags = "套餐管理相关接口")
@RequestMapping("/admin/setmeal")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @ApiOperation("根据id查询套餐")
    @GetMapping("/{id}")
    public Result<SetmealVO> selectById(@PathVariable Long id)
    {
        log.info("根据id: {} 查询套餐", id);
        SetmealVO setmealVO = setMealService.selectById(id);
        return Result.success(setmealVO);
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result insert(@RequestBody SetmealDTO setmealDTO)
    {
        log.info("新增套餐: {}", setmealDTO);
        setMealService.insertWithDishes(setmealDTO);
        return Result.success();
    }
}
