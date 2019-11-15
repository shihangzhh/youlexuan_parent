package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清理redis缓存
		redisTemplate.delete("content");
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//获取原来广告所属类目
		TbContent contentSrc = contentMapper.selectByPrimaryKey(content.getId());
		Long categoryIdSrc = contentSrc.getCategoryId();

		//清理原来类目所对应缓存
		redisTemplate.boundHashOps("content").delete(categoryIdSrc);

		//判断新修改类目id和原来类目id是否一致
		if(!content.getCategoryId().equals(categoryIdSrc)){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}


		contentMapper.updateByPrimaryKey(content);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbContent content = contentMapper.selectByPrimaryKey(id);
			Long categoryId = content.getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}
	}


		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();

		if(content!=null){
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
		}

		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		//1、先从redis缓存查询读取数据
		List<TbContent> list=(List<TbContent>)redisTemplate.boundHashOps("content").get(categoryId);

     if(list==null) {
		 TbContentExample example = new TbContentExample();
		 Criteria criteria = example.createCriteria();

		 criteria.andCategoryIdEqualTo(categoryId);
		 //设置根据排序字段排序
		 example.setOrderByClause("sort_order");

		 list= contentMapper.selectByExample(example);
		 //把从数据库读取到数据，写入到redis缓存
		 redisTemplate.boundHashOps("content").put(categoryId,list);
	 }else {
		 System.out.println("从缓存读取到了数据");
	 }

     return  list;
	}
}
