package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //向订单表插入一条数据（地址簿为空，购物车数据为空，不允许下单）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectShoppingCartList(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单明细表插入n条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetails = new ArrayList<>();
        //如果下单成功，清空购物车
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的订单id
            orderDetails.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetails);

        shoppingCartMapper.deleteByUserId(userId);

        //封装vo返回结果

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;
        paySuccess(ordersPaymentDTO.getOrderNumber());



        String orderNumber = ordersPaymentDTO.getOrderNumber(); //订单号

        Long orderId = orderMapper.getOrderId(orderNumber);//根据订单号查主键



        JSONObject jsonObject = new JSONObject();//本来没有2

        jsonObject.put("code", "ORDERPAID"); //本来没有3

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);

        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改

        Integer orderPaidStatus = Orders.PAID; //支付状态，已支付

        Integer orderStatus = Orders.TO_BE_CONFIRMED; //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新

        LocalDateTime now = LocalDateTime.now();



        orderMapper.updateStatus(orderStatus, orderPaidStatus, now, orderId);

        return vo;  //  修改支付方法中的代码
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 查询订单具体信息
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderInfo(Long id) {
        OrderVO orderVO = new OrderVO();
        //获取订单信息
        Orders orders = orderMapper.selectByOrderId(id);
        BeanUtils.copyProperties(orders, orderVO);
        //将查询订单详情表
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 查询历史订单信息
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        Page<Orders> page = orderMapper.selectOrdersByUserId(ordersPageQueryDTO);

        //批量获取数据
        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders orders : page) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(orders.getId());
            orderVO.setOrderDetailList(orderDetails);
            orderVOList.add(orderVO);
        }

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    @Transactional
    public void cancelOrder(Long id) {
        //从orders表中删除该id对应的订单数据
        orderMapper.cancelOrderById(id);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    @Transactional
    public void repetitionOrder(Long id) {
        Orders orders = orderMapper.selectByOrderId(id);
        Orders newOrder = new Orders();
        BeanUtils.copyProperties(orders, newOrder);
        newOrder.setId(null);
        newOrder.setOrderTime(LocalDateTime.now());
        newOrder.setPayStatus(Orders.UN_PAID);
        newOrder.setStatus(Orders.PENDING_PAYMENT);
        newOrder.setNumber(String.valueOf(System.currentTimeMillis()));
        orderMapper.insert(newOrder);

        //获取所有orders的order_detail
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderId(id);
        List<OrderDetail> newOrderDetails = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            //将id设置为null
            orderDetail.setId(null);
            //将对应的订单id设置为newOrderId
            orderDetail.setOrderId(newOrder.getId());
            newOrderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(newOrderDetails);

    }
}
