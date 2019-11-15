package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Cart;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
    private TbOrderItemMapper orderItemMapper;

	@Autowired
    private IdWorker idWorker;

	@Autowired
    private RedisTemplate<String,Object> redisTemplate;

	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
     * 订单生成的步骤
     * 购物车列表------》商家购物车-----》商家订单---》商家订单明细
     * 一个卖家对应一辆购物车  同样对应一张订单 及 一张订单详情
     * 购物车列表中可能含有多个卖家，所以要对应多个多张订单
	 */
	@Override
	public void add(TbOrder order) {

		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		//获取订单列表
		List<String> orderIdList = new ArrayList<>();
		//创建付款总金额
		double total_money = 0.0;
		if (cartList == null) {
			throw new RuntimeException("您的购物车为空哦");
		}
		for (Cart cart : cartList) {

			//生成订单id
			long orderId = idWorker.nextId();

			System.out.println("卖家的id是" + cart.getSellerId());
			//创建订单

			TbOrder tborder = new TbOrder();
			tborder.setOrderId(orderId);// 订单 ID

			tborder.setUserId(order.getUserId());// 用户名
			tborder.setPaymentType(order.getPaymentType());// 支付类型
			tborder.setStatus("1");// 状态：未付款
			tborder.setCreateTime(new Date());// 订单创建日期
			tborder.setUpdateTime(new Date());// 订单更新日期
			tborder.setReceiverAreaName(order.getReceiverAreaName());// 地址
			tborder.setReceiverMobile(order.getReceiverMobile());// 手机号
			tborder.setReceiver(order.getReceiver());// 收货人
			tborder.setSourceType(order.getSourceType());// 订单来源
			tborder.setSellerId(cart.getSellerId());// 商家 ID

			double money = 0.0;
			//循环购物车明细

			for (TbOrderItem orderItem : cart.getOrderItemList()) {

				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);// 订单 ID
				orderItem.setSellerId(cart.getSellerId());
				money += orderItem.getTotalFee().doubleValue();// 金额累加

				//将订单id放入到订单集合中
				orderIdList.add(orderId + "");
				total_money += money;

				orderItemMapper.insert(orderItem);
			}
			tborder.setPayment(new BigDecimal(money));
			//将订单保存到数据库里面
			orderMapper.insert(tborder);
		}
	//如果是支付宝支付
			TbPayLog payLog = new TbPayLog();
			String outTradeNo = idWorker.nextId() + "";//支付订单号
			payLog.setOutTradeNo(outTradeNo);//支付订单号
			payLog.setCreateTime(new Date());//创建时间
//订单号列表，逗号分隔
			String ids = orderIdList.toString().replace("[", "").replace("]",
					"").replace(" ", "");
			payLog.setOrderList(ids);//订单号列表，逗号分隔
			payLog.setPayType("1");//支付类型
//把元转换成分
			System.out.println("合计金额:" + total_money);
			BigDecimal total_money1 = BigDecimal.valueOf(total_money);
			BigDecimal cj = BigDecimal.valueOf(100d);
//高精度乘法
			BigDecimal bigDecimal = total_money1.multiply(cj);
			double hj=total_money*100;
			System.out.println("合计:"+hj);
			System.out.println("高精度处理:"+bigDecimal.toBigInteger().longValue());
			payLog.setTotalFee(bigDecimal.toBigInteger().longValue());
			payLog.setTradeState("0");//支付状态
			payLog.setUserId(order.getUserId());//用户 ID
			payLogMapper.insert(payLog);//插入到支付日志表
			redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存

			//从缓存redis删除该用户的购物车
			redisTemplate.boundHashOps("cartList").delete(order.getUserId());

	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	@Override  //从缓存中查找支付日志
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {

		//1.修改支付日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//已支付
		payLog.setTransactionId(transaction_id);//交易号
		payLogMapper.updateByPrimaryKey(payLog);
//2.修改订单状态
		String orderList = payLog.getOrderList();//获取订单号列表
		String[] orderIds = orderList.split(",");//获取订单号数组

		for(String orderId:orderIds){
			TbOrder order = orderMapper.selectByPrimaryKey( Long.parseLong(orderId) );
			if(order!=null){
				order.setStatus("2");//已付款
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//清除 redis 缓存数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}
}
