package com.offcn.seckill.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckill_order);


	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckill_order);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckill_order, int pageNum, int pageSize);

	/**
	 * 秒杀下单
	 * @param seckillId 秒杀商品id
	 * @param userId  用户id
	 */

	public void submitOrder(Long seckillId,String userId);

	/**
	 * 根据用户名查找订单
	 * @param userId
	 * @return
	 */
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId);

    /**
     * 用户付完款之后，将订单保存到数据库中，并将该用户的订单从数据库中删除
     * @param userId
     * @param orderId
     * @param transactionId
     */
    public void saveOrderFromRedisToDb(String userId,Long orderId,String transactionId);


    /**
     * 用户在三十秒内未完成付款，则订单从缓存中删除
     * @param userId
     * @param orderId
     */
    public void deleteOrderFromRedis(String userId,String orderId);


}
