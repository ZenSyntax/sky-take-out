package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    @Cacheable(cacheNames = "historyOrders", key = "#ordersPageQueryDTO.status != null ? #ordersPageQueryDTO.status : 0")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("将要搜索的订单信息为：{}", ordersPageQueryDTO);
        PageResult orderVO = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(orderVO);
    }

    /**
     * 订单信息统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("订单状态数量统计")
    @Cacheable(cacheNames = "orderStatistics", key = "0")
    public Result<OrderStatisticsVO> orderStatistics() {
        log.info("检索订单统计信息");
        OrderStatisticsVO orderStatistics = orderService.orderStatistics();
        return Result.success(orderStatistics);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    @Cacheable(cacheNames = "orderDetail", key = "#id")
    public Result<OrderVO> orderDetails(@PathVariable Long id) {
        log.info("将要查询的订单为：{}", id);
        OrderVO vo = orderService.getOrderInfo(id);
        return Result.success(vo);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    @Caching(evict = {
            @CacheEvict(cacheNames = "historyOrders", allEntries = true),
            @CacheEvict(cacheNames = "orderDetails", key = "#ordersConfirmDTO.id"),
            @CacheEvict(cacheNames = "orderStatistics", key = "0")
    })
    public Result confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单：{}", ordersConfirmDTO);
        orderService.orderConfirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    @Caching(evict = {
            @CacheEvict(cacheNames = "historyOrders", allEntries = true),
            @CacheEvict(cacheNames = "orderDetails", key = "#ordersRejectionDTO.id"),
            @CacheEvict(cacheNames = "orderStatistics", key = "0")
    })
    public Result rejectionOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        OrdersCancelDTO ordersCancelDTO = new OrdersCancelDTO();
        ordersCancelDTO.setId(ordersRejectionDTO.getId());
        ordersCancelDTO.setCancelReason(ordersRejectionDTO.getRejectionReason());
        log.info("拒单：{}", ordersRejectionDTO);
        orderService.orderRejection(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    @Caching(evict = {
            @CacheEvict(cacheNames = "historyOrders", allEntries = true),
            @CacheEvict(cacheNames = "orderDetails", key = "#ordersCancelDTO.id"),
            @CacheEvict(cacheNames = "orderStatistics", key = "0")
    })
    public Result cancelOrder (@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("将要取消订单：{}", ordersCancelDTO);
        orderService.adminCancelOrder(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    @Caching(evict = {
            @CacheEvict(cacheNames = "historyOrders", allEntries = true),
            @CacheEvict(cacheNames = "orderDetails", key = "#id"),
            @CacheEvict(cacheNames = "orderStatistics", key = "0")
    })
    public Result deliveryOrder(@PathVariable Long id) {
        log.info("将要派送的订单：{}", id);
        orderService.deliveryOrder(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    @Caching(evict = {
            @CacheEvict(cacheNames = "historyOrders", allEntries = true),
            @CacheEvict(cacheNames = "orderDetails", key = "#id"),
            @CacheEvict(cacheNames = "orderStatistics", key = "0")
    })
    public Result completeOrder(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        orderService.completeOrder(id);
        return Result.success();
    }
}
