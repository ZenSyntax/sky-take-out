package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

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

    Page<Orders> selectOrdersByUserId(OrdersPageQueryDTO ordersPageQueryDTO);

    @Delete("delete from orders where id = #{id}")
    void deleteById(Long id);

    @Update("update orders set status = 6 where id = #{id}")
    void cancelOrderById(Long id);
}
