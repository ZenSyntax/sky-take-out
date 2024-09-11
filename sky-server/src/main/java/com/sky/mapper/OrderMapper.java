package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.*;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 获取orderid的mapper层方法，写在OrderMapper.java文件下
     * @param orderNumber
     * @return
     */
    @Select("select * from orders where number=#{orderNumber}")
    Long getOrderId(String orderNumber);

    /**
     * 用于替换微信支付更新数据库状态的问题
     * @param orderStatus
     * @param orderPaidStatus
     */
    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{now} where id = #{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime now, Long id);

    /**
     * 获取订单信息
     * @param id
     */
    @Select("select * from orders where id = #{id}")
    Orders selectByOrderId(Long id);

    /**
     * 分页查询订单信息
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> selectOrdersByUserId(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 取消订单
     * @param id
     */
    @Update("update orders set status = 6 where id = #{id}")
    void cancelOrderById(Long id);

    /**
     * 统计某一状态下的订单数据
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    int selectStatusTol(Integer status);

    /**
     * 更新订单状态
     * @param ordersConfirmDTO
     */
    @Update("update orders set status = #{status} where id = #{id}")
    void updateOrderStatus(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Update("update orders set status = 7, rejection_reason = #{rejectionReason} where id = #{id}")
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理员取消订单
     * @param ordersCancelDTO
     */
    @Update("update orders set status = 6, cancel_reason = #{cancelReason}, cancel_time=now() where id = #{id}")
    void adminCancelOrderById(OrdersCancelDTO ordersCancelDTO);

    /**
     * 根据订单状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 查询某天营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件查询订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 统计指定时间区间内的销量排名前十
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
