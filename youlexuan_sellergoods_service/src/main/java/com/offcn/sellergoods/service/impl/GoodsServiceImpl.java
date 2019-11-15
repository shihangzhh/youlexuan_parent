package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Transactional
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemMapper itemMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//设置商品状态为 待审核
		goods.getGoods().setAuditStatus("0");
		//保存商品基本信息
		goodsMapper.insert(goods.getGoods());

		//出现错误
		try {
			int x=10;
			x=x/0;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("出现错误");
		}
		//保存商品扩展信息
		   //关联商品扩展信息和spuid
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());

		//sku数据保存
		saveItemList(goods);

	}

	//保存sku列表数据
	private void saveItemList(Goods goods){
		//判断规格是否启用
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			//1、循环遍历sku集合
			for (TbItem item : goods.getItemList()) {
				//加工处理标题
				//获取sku基本商品标题
				String title = goods.getGoods().getGoodsName();
				//获取规格数据 {"机身内存":"16G","网络":"移动3G"}
				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				//遍历规格map
				for (String key : specMap.keySet()) {
					title += " " + specMap.get(key);
				}
				//设置标题给item
				item.setTitle(title);
				//设置sku属性
				setItemValues(goods,item);

				//保存sku数据到数据库

				itemMapper.insert(item);
			}
		}else {
			//不启用规格
			//创建sku对象
			TbItem item = new TbItem();
			//设置标题
			item.setTitle(goods.getGoods().getGoodsName());
			//设置价格
			item.setPrice(goods.getGoods().getPrice());
			//设置商品状态
			item.setStatus("1");
			//设置库存
			item.setNum(99999);
			//设置是否默认
			item.setIsDefault("1");
			//设置规格
			item.setSpec("{}");
			//设置sku属性
			setItemValues(goods,item);
			itemMapper.insert(item);


		}
	}

	public void setItemValues(Goods goods,TbItem item){
		//管理商品编号
		item.setGoodsId(goods.getGoods().getId());
		//关联卖家id
		item.setSellerId(goods.getGoods().getSellerId());
		//关联类目id
		item.setCategoryid(goods.getGoods().getCategory3Id());
		//设置创建时间
		item.setCreateTime(new Date());
		//设置更新时间
		item.setUpdateTime(new Date());
		//关联品牌
		Long brandId = goods.getGoods().getBrandId();
		TbBrand brand = brandMapper.selectByPrimaryKey(brandId);
		item.setBrand(brand.getName());
		//关联所属分类名称
		Long category3Id = goods.getGoods().getCategory3Id();
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(category3Id);
		item.setCategory(itemCat.getName());
		//商家名称
		String sellerId = goods.getGoods().getSellerId();
		TbSeller seller = sellerMapper.selectByPrimaryKey(sellerId);
		item.setSeller(seller.getNickName());
		//读取商品配图
		List<Map> imagesList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imagesList != null && imagesList.size() > 0) {
			//获取配图第一张设置到sku
			item.setImage((String) imagesList.get(0).get("url"));
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改商品状态为待审核
		goods.getGoods().setAuditStatus("0");
		//更新商品基本信息
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新商品扩展信息
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//删除现有sku列表数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		//新增sku列表保存到数据库
		saveItemList(goods);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		//查询商品扩展信息
		goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));

		//sku列表数据待处理
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> list = itemMapper.selectByExample(example);
		goods.setItemList(list);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//逻辑删除掉了 spu
		for(Long id:ids){
			//修改商品基本信息的删除状态
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改删除状态，标记为1 表示逻辑删除
			goods.setIsDelete("1");
			//更新保存到数据库
			goodsMapper.updateByPrimaryKey(goods);
		}
		//修改sku商品状态为删除
		 //获取指定商品编号对应sku数据集合
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		List<TbItem> itemList = itemMapper.selectByExample(example);
		//遍历sku列表
		for (TbItem item : itemList) {
			item.setStatus("3");
			itemMapper.updateByPrimaryKey(item);
		}
	}


		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		if(goods!=null){
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}		/*	if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}*/
			criteria.andIsDeleteIsNull();
		}

		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			//根据商品编号获取对应商品信息
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品状态
			goods.setAuditStatus(status);
			//更新商品信息到数据库
			goodsMapper.updateByPrimaryKey(goods);

			//根据商品编号，查询对应sku列表
			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(id);
			List<TbItem> list = itemMapper.selectByExample(example);
			//遍历sku列表
			for (TbItem item : list) {
				//修改sku状态
				item.setStatus(status);
				//更新sku数据到数据库
				itemMapper.updateByPrimaryKey(item);
			}
		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();

		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);
	}
}
