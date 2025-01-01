package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.BaseException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;
import org.apache.poi.hssf.record.chart.SheetPropertiesRecord;
import org.apache.poi.sl.usermodel.SlideShow;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.awt.geom.RectangularShape;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;


    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //1. 检查地址和购物车是否为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null)
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectExist(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty())
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);

        //2. 将一条数据插入到orders表
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        //完善订单数据
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        //3. 将n条数据插入到order_detail表
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);

        //4. 清空购物车
        shoppingCartMapper.clean(userId);
        //5. 返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
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
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //这里我们跳过与微信服务器的交互
        //调用微信支付接口，生成预支付交易单
/*        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/

        //直接生成空的jsonObject
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
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
     * 查询历史订单信息
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //1.先查询订单的信息
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Long userId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(userId);
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        //2.再查询订单关联的订单详情信息
        List<OrderVO> result = new ArrayList<>();
        long total = page.getTotal();
        for (Orders orders : page.getResult()) {
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrdersId(orders.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetails);
            result.add(orderVO);
        }

        return new PageResult(total, result);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO selectByOrderId(Long id) {
        Orders orders = orderMapper.selectById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrdersId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) {
        //1. 先判断订单是否存在，不存在则抛异常
        Orders orders = orderMapper.selectById(id);
        if (orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        //2. 如果订单状态大于2，那么需要电话联系商家
        if (orders.getStatus() > 2)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //3. 否则修改订单状态为已取消
        Orders orders1 = new Orders();
        orders1.setId(orders.getId());
        orders1.setCancelReason("用户取消");
        orders1.setCancelTime(LocalDateTime.now());
        orders1.setStatus(Orders.CANCELLED);
        orderMapper.update(orders1);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void buyAgain(Long id) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrdersId(id);
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        if (orderDetails != null && !orderDetails.isEmpty()) {
            for (OrderDetail orderDetail : orderDetails) {
                ShoppingCart shoppingCart = new ShoppingCart();
                BeanUtils.copyProperties(orderDetail, shoppingCart);
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.insert(shoppingCart);
            }
        }
    }

    /**
     * 条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOS = new ArrayList<>();
        for (Orders orders : page.getResult()) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrdersId(orders.getId());
            List<String> dishBrives = new ArrayList<>();
            orderDetails.forEach(orderDetail -> {
                String dishBrief = orderDetail.getName() + "*" + orderDetail.getNumber();
                dishBrives.add(dishBrief);
            });
            String s = String.join(";", dishBrives);
            orderVO.setOrderDishes(s);
            orderVOS.add(orderVO);
        }

        return new PageResult(page.getTotal(), orderVOS);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO countStatus() {
        Integer confirmed = orderMapper.selectStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.selectStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = orderMapper.selectStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .toBeConfirmed(toBeConfirmed)
                .build();
        return orderStatisticsVO;
    }

    /**
     * 查询订单详情-管理端
     * @param id
     * @return
     */
    @Override
    public OrderVO searchDetail(Long id) {
        OrderVO orderVO = selectByOrderId(id);
        List<OrderDetail> orderDetails = orderVO.getOrderDetailList();
        List<String> dishBrives = new ArrayList<>();
        orderDetails.forEach(orderDetail -> {
            String dishBrief = orderDetail.getName() + "*" + orderDetail.getNumber();
            dishBrives.add(dishBrief);
        });
        String s = String.join(";", dishBrives);
        orderVO.setOrderDishes(s);
        return orderVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //1.先判断订单状态是否为待接单，不是则抛异常
        Orders orders = orderMapper.selectById(ordersRejectionDTO.getId());
        if (orders == null)
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);

        if (orders.getStatus() == null || orders.getStatus() != Orders.TO_BE_CONFIRMED)
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        //2.设置订单的拒单原因
        Orders orders1 = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();

        //3.更新数据库订单数据
        orderMapper.update(orders1);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .build();

        orderMapper.update(orders);
    }
}
