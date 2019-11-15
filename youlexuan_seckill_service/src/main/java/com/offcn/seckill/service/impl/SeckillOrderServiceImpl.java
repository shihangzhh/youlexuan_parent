package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
    private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

    /**
     * 在短时间内多人秒杀产品的时候，有可能造成多个用户同时抢购同一件商品的情况，所以要进行事务性的约束
     * @param seckillId 秒杀商品id
     * @param userId  用户id
     */
	@Override
	public void submitOrder(Long seckillId, String userId) {

	    //允许redis使用时事务
        redisTemplate.setEnableTransactionSupport(true);

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                //监控seckillGoods
                redisOperations.watch("seckillGoods");

                //在执行事务前执行查询，获取秒杀对象，
                TbSeckillGoods seckillGoods  = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
                //开始事务
                redisOperations.multi();
                //必要的空查询
                redisOperations.boundHashOps("seckillGoods").get(seckillId);
                if (seckillGoods == null){

                    throw  new RuntimeException("秒杀的商品不存在"+seckillId);
                }

                if (seckillGoods.getNum() <=0){

                    throw  new RuntimeException("商品已经卖完了");
                }

                //减少库存
                seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

                //把商品信息添加到缓存数据库
                redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);

                //如果库存容量小于0的话，则将信息同步到数据库中，同时将缓存中的商品删除
                if (seckillGoods.getStockCount()==0 ){
                    seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                    redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
                }

                //保存订单
                TbSeckillOrder seckillOrder = new TbSeckillOrder();

                //订单id
                long orderId = idWorker.nextId();
                seckillOrder.setId(orderId);
                //设置秒杀产品id
                seckillOrder.setSeckillId(seckillId);
                seckillOrder.setMoney(seckillGoods.getCostPrice());
                seckillOrder.setUserId(userId);
                seckillOrder.setCreateTime(new Date());
                seckillOrder.setSellerId(seckillGoods.getSellerId());
                //订单状态为0
                seckillOrder.setStatus("0");
             redisOperations.boundHashOps("seckillOrder").put(userId,seckillOrder);
                return redisOperations.exec();
            }
        });

    }

    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    /**
     *
     * @param userId
     * @param orderId
     * @param transactionId
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {

        //根据用户id从缓存中查询出来订单

        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

        if (seckillOrder == null){

            throw  new RuntimeException("您的订单不存在");
        }

        if (seckillOrder.getId().longValue() != orderId.longValue()) {

            throw new RuntimeException("订单不相符");
        }

        seckillOrder.setTransactionId(transactionId);//交易流水号
        seckillOrder.setPayTime(new Date());//支付时间
        seckillOrder.setStatus("1");//状态
         seckillOrderMapper.insert(seckillOrder);//保存到数据库
         redisTemplate.boundHashOps("seckillOrder").delete(userId);//从 redis 中清除
    }

    @Override
    public void deleteOrderFromRedis(String userId, String orderId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

        if (seckillOrder !=null && String.valueOf(seckillOrder.getId()) == orderId){

            //将订单从缓存中删除
            redisTemplate.boundHashOps("seckillOrder").delete(userId);

            //恢复库存
           TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());

           if (seckillGoods != null){
               seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
               redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
           }



        }

    }
}
