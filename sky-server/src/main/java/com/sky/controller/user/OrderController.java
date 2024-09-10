package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    @CacheEvict(cacheNames = "historyOrders", allEntries = true)
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单，参数为：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    @Caching(evict = {
            @CacheEvict(cacheNames = "historyOrders", allEntries = true),
            @CacheEvict(cacheNames = "orderDetails", allEntries = true)
    })
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询历史订单信息
     * @return
     * @throws Exception
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    @Cacheable(cacheNames = "historyOrders", key = "#ordersPageQueryDTO.status != null ? #ordersPageQueryDTO.status : 0")
    public Result getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.getHistoryOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 获取订单详细信息
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("获取订单详细信息")
    @Cacheable(cacheNames = "orderDetail", key = "#id")
    public Result<OrderVO> getOrderInfo (@PathVariable Long id) {
        log.info("将要查询的订单为：{}", id);
        OrderVO vo = orderService.getOrderInfo(id);
        return Result.success(vo);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    @Caching(evict = {
            @CacheEvict(cacheNames = "historyOrders", allEntries = true),
            @CacheEvict(cacheNames = "orderDetails", key = "#id")
    })
    public Result cancelOrder (@PathVariable Long id) {
        log.info("将要取消的订单id为：{}", id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    @CacheEvict(cacheNames = "historyOrders", allEntries = true)
    public Result repetitionOrder (@PathVariable Long id) {
        log.info("再来一单id为：{}", id);
        orderService.repetitionOrder(id);
        return Result.success();
    }
}
