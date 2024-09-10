package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单数据
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 获取订单细节信息
     * @param id
     * @return
     */
    @Select("select * from order_detail where order_id = #{id}")
    List<OrderDetail> selectByOrderId(Long id);

    /**
     * 根据订单id删除所有订单信息数据
     * @param id
     */
    @Delete("delete from order_detail where order_id = #{id}")
    void deleteByOrderId(Long id);
}
