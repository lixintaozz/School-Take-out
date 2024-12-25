package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.RecursiveAction;

@RestController
@Api(tags = "C端-购物车相关接口")
@Slf4j
@RequestMapping("/user/shoppingCart/")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车接口")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO)
    {
        log.info("添加到购物车的商品为: {}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     * @return
     */
    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list()
    {
        log.info("查看购物车...");
        List<ShoppingCart> shoppingCartList = shoppingCartService.list();
        return Result.success(shoppingCartList);
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clean()
    {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }
}
