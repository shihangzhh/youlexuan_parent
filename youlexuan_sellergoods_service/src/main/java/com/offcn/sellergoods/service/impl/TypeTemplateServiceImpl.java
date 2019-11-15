package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.mapper.TbTypeTemplateMapper;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.pojo.TbTypeTemplate;
import com.offcn.pojo.TbTypeTemplateExample;
import com.offcn.pojo.TbTypeTemplateExample.Criteria;
import com.offcn.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}
	}


		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();

		if(typeTemplate!=null){
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
		}

		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
			//PageInfo<TbTypeTemplate> pageInfo = new PageInfo<>(typeTemplateMapper.selectByExample(example));
			//保存全部模板对应的品牌和规格数据到redis缓存
			saveToRedis();
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findSpecList(Long id) {
		//根据模板id获取模板对象
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//获取该模板对象里面包含的规格，转换成对象
		//[{"id":26,"text":"尺码"},{"id":28,"text":"手机屏幕尺寸"}]
		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
		if(list!=null){
			for (Map map : list) {
				//获取规格id
				Long specId =new Long((Integer) map.get("id")) ;
				//编写规格选项查询条件
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				TbSpecificationOptionExample.Criteria criteria = example.createCriteria();

				criteria.andSpecIdEqualTo(specId);


				List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);
				map.put("options",optionList);
			}
		}
		//[{"id":26,"text":"尺码","options":[｛"optionName":"4.0"},]},{"id":28,"text":"手机屏幕尺寸"}]
		return list;
	}

	//保存品牌和规格到redis缓存方法
	private void saveToRedis(){
		//1、获取全部的模板数据
		List<TbTypeTemplate> templateList = findAll();
		//2、遍历模板集合
		for (TbTypeTemplate tbTypeTemplate : templateList) {
			//从模板对象获取对应品牌
			List<Map> listBrand = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);
			//把品牌数据写入到redis缓存
			redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(),listBrand);

			//根据模板id获取扩展后的规格和规格选项集合
			List<Map> specList = findSpecList(tbTypeTemplate.getId());
			//吧规格以及对应规格选项的集合写入到redis缓存
			redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(),specList);


		}
	}
}
