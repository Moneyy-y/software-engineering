package com.catering.controller;

import com.catering.common.PageResult;
import com.catering.common.Result;
import com.catering.entity.Dish;
import com.catering.entity.Shop;
import com.catering.service.DishService;
import com.catering.vo.DishDetailVO;
import com.catering.vo.DishVO;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/dish/list")
    public Result<PageResult<DishVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(dishService.listDishes(keyword, shopId, category, minPrice, maxPrice,
                sortBy, lat, lng, page, size));
    }

    @GetMapping("/dish/{id}")
    public Result<DishDetailVO> detail(@PathVariable Long id,
                                       @RequestParam(required = false) Double lat,
                                       @RequestParam(required = false) Double lng) {
        return Result.ok(dishService.getDetail(id, lat, lng));
    }

    @GetMapping("/shop/list")
    public Result<List<Shop>> shops() {
        return Result.ok(dishService.listShops());
    }

    @PostMapping("/admin/dish/save")
    public Result<Dish> saveDish(@RequestBody Dish dish) {
        return Result.ok(dishService.saveDish(dish));
    }

    @PostMapping("/admin/dish/delete")
    public Result<Void> deleteDish(@RequestParam Long dishId) {
        dishService.deleteDish(dishId);
        return Result.ok();
    }
}
