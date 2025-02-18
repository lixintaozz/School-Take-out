package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@Api(tags = "店铺相关接口")
@RequestMapping("/admin/shop")
public class ShopController {
    public static final String SHOP_STATUS = "shop_status";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation("获取店铺营业状态")
    @GetMapping("/status")
    public Result<Integer> getShopStatus()
    {
        log.info("获取店铺营业状态...");
        Integer status = Integer.valueOf(stringRedisTemplate.opsForValue().get(SHOP_STATUS));
        return Result.success(status);
    }

    @ApiOperation("设置店铺营业状态")
    @PutMapping("/{status}")
    public Result setShopStatus(@PathVariable Integer status)
    {
        log.info("设置店铺营业状态为: {}", status == 1 ? "营业中" : "已打烊");
        stringRedisTemplate.opsForValue().set(SHOP_STATUS, status.toString());
        return Result.success();
    }
}
