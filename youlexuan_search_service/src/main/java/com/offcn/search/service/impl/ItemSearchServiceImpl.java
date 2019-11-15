package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map=new HashMap<>();

     /*   //1、创建查询器对象
        SimpleQuery query = new SimpleQuery();
        //2、创建查询条件对象
        Criteria criteria = new Criteria("item_keyword").is(searchMap.get("keywords"));

        //3、关联查询条件到查询器对象
        query.addCriteria(criteria);

        //4、发出查询
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(query, TbItem.class);

        //5、把查询到结果封装到返回map
        map.put("rows",scoredPage.getContent());*/
       //1、根据关键字查询
       map.putAll(searchList(searchMap));

       //2、根据查询关键字获取对应分类
        List categoryList = searchCategoryList(searchMap);
      map.put("categoryList",categoryList);

      //3、根据分类名称获取对应品牌和规格
        //判断前端传递查询条件是否包含类目
        if(!"".equals(searchMap.get("category"))){
            //按照传递类目来获取对应规格和品牌
           map.putAll(searchBrandAndSpecList((String) searchMap.get("category")));
        }else {
            if(categoryList!=null&&categoryList.size()>0) {
                map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));   ;
            }
        }



        return map;
    }

    //定义一个支持高亮查询结果方法
    private Map searchList(Map searchMap){

        Map<String, Object> map=new HashMap<>();

        //1、创建支持高亮查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2、创建高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions();
         //设置高亮域
        highlightOptions.addField("item_title");
        //设置高亮样式前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮样式后缀
        highlightOptions.setSimplePostfix("</em>");
        //高亮选项关联高亮查询器对象
        query.setHighlightOptions(highlightOptions);

        //3、设置查询条件
        //判断关键字不为空，也不是空格，在检索里面是否包含空格

        if(searchMap.get("keywords")!=null&&!searchMap.get("keywords").equals("")&&searchMap.get("keywords").toString().indexOf(" ")>0){
          searchMap.put("keywords",searchMap.get("keywords").toString().replace(" ",""));
        }

        Criteria criteria = new Criteria("item_keyword").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //3.1 根据商品所属类目进行查询
         if(!"".equals(searchMap.get("category"))){
             //设置查询条件
             Criteria criteriaCategory = new Criteria("item_category").is(searchMap.get("category"));
             //创建过滤器对象
             SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
             //关联查询条件到过滤器对象
             simpleFilterQuery.addCriteria(criteriaCategory);
             //把过滤器对象关联到主查询器对象
             query.addFilterQuery(simpleFilterQuery);
         }

         //3.2 根据商品品牌进行查询
        if(!"".equals(searchMap.get("brand"))){
            Criteria criteriaBrand = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
            simpleFilterQuery.addCriteria(criteriaBrand);
            query.addFilterQuery(simpleFilterQuery);
        }

        //3.3 根据商品所属规格以及对应规格选项进行查询
        if(searchMap.get("spec")!=null){
            //{"网络":"3G","内存":"16G"}
         Map mapspec=(Map)searchMap.get("spec");
         //遍历规格map
            if(mapspec!=null) {
                for (Object key : mapspec.keySet()) {
                    Criteria criteriaSpec = new Criteria("item_spec_" + Pinyin.toPinyin(key.toString(), "").toLowerCase()).is(mapspec.get(key));
                    SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                    filterQuery.addCriteria(criteriaSpec);
                    query.addFilterQuery(filterQuery);
                }
            }
        }
        //3.4 根据用户选择的价格区间进行过滤
        if(!"".equals(searchMap.get("price"))){
            // 0-500  500-1000  。。。。 3000-*
          String priceStr=(String)  searchMap.get("price");
            String[] price = priceStr.split("-");
            if(price!=null&&!price[0].equals("0")){
                //设置价格开始条件  价格 >=开始价格
                Criteria item_priceCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                simpleFilterQuery.addCriteria(item_priceCriteria);
                query.addFilterQuery(simpleFilterQuery);

            }
            //添加结束价格
            if(price!=null&&!price[1].equals("*")){
                Criteria item_priceCriteria = new Criteria("item_price").lessThan(price[1]);
                SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
                simpleFilterQuery.addCriteria(item_priceCriteria);
                query.addFilterQuery(simpleFilterQuery);
            }
        }


        //3.5添加分页处理

         //3.5.1  获取前端传递过来 当前页码
      Integer pageNo=(Integer)  searchMap.get("pageNo");
        //判断pageNo如果为空，设置一个默认值
        if(pageNo==null){
            pageNo=1;
        }
        //3.5.2 获取前端传递过来的  每页显示的记录数
        Integer pageSize=(Integer) searchMap.get("pageSize");
        if(pageSize==null){
            pageSize=20;
        }
           //设置游标开始位置 (1--->0  2--->(pageNo-1)*pageSize)
         Integer start=(pageNo-1)*pageSize;
        query.setOffset(start);
          //设置每页显示记录数 20条
        query.setRows(pageSize);

        //3.6 按照指定字段对搜索结果进行排序
           //要排序的字段名称
         String sortField=(String) searchMap.get("sortField");
            //排序方式  ASC\DESC
        String sort=(String)searchMap.get("sort");
        if(sortField!=null&&sort!=null&&!sort.equals("")&&!sortField.equals("")){
            if(sort.equals("ASC")){
                //创建排序对象
                Sort sortAsc = new Sort(Sort.Direction.ASC, "item_" + sortField);
                //关联排序对象到查询器对象
                query.addSort(sortAsc);
            }
            if(sort.equals("DESC")){
                Sort sortDesc = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sortDesc);
            }
        }

        //4、发出查询
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //5、获取查询到结果记录集合
        List<TbItem> list = highlightPage.getContent();
        //获取满足查询条件的总记录数
        long totalElements = highlightPage.getTotalElements();
        System.out.println("查询总记录数:"+totalElements);

        //6、遍历查询结果集合
        for (TbItem item : list) {
            //获取到针对对象TbItem高亮集合
            List<HighlightEntry.Highlight> highlights = highlightPage.getHighlights(item);
            if(highlights!=null&&highlights.size()>0) {
                //获取第一个字段高亮对象
                List<String> highlightSnipplets = highlights.get(0).getSnipplets();
                System.out.println("高亮：" + highlightSnipplets.get(0));
                //使用高亮结果替换商品标题
                item.setTitle(highlightSnipplets.get(0));
            }
        }

        map.put("rows",list);

         //总页数
        map.put("totalPages",highlightPage.getTotalPages());
        //满足查询条件的总记录数
        map.put("total",highlightPage.getTotalElements());

        return  map;


    }

    //根据指定搜索关键字，获取对应分类
    private List searchCategoryList(Map searchMap){
        List listReturn=new ArrayList();
        //1、创建查询器对象
        SimpleQuery query = new SimpleQuery();
        //2、创建查询条件对象
        Criteria criteria = new Criteria("item_keyword").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //3、设置分组字段 分组配置项对象
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //关联到查询器对象
        query.setGroupOptions(groupOptions);

        //4、发出查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);

        //5、获取分组入口
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");

        //6、针对分组字段item_category分组封装，获取对应对象
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

        //7、获取分组封装
        List<GroupEntry<TbItem>> list = groupEntries.getContent();

        //8、遍历封装集合
        for (GroupEntry<TbItem> entry : list) {
            listReturn.add(entry.getGroupValue());
        }

        return listReturn;
    }

    //根据分类名称获取对应品牌、规格
    public Map searchBrandAndSpecList(String categoryName){
       Map map=new HashMap();
        //从缓存根据分类名称读取到对应模板id
        Long typeId =(Long) redisTemplate.boundHashOps("itemCat").get(categoryName);

        if(typeId!=null) {
            //根据模板id从redis缓存获取对应品牌
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);

            //根据模板id从redis缓存中获取对应规格
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }

      return map;

    }

    @Override
    public void importList(List<TbItem> list) {
        //循环遍历sku集合
        for (TbItem item : list) {
            //获取规格属性
            Map mapSpec = JSON.parseObject(item.getSpec(), Map.class);
            //定义一个map存储key为拼音的规格 value 为对应的规格选项
            Map mapSpecPinyin=new HashMap();
            //遍历规格map，转换key从中文转换为拼音
            for (Object key : mapSpec.keySet()) {
                mapSpecPinyin.put("item_spec_"+Pinyin.toPinyin(key.toString(),"").toLowerCase(),mapSpec.get(key));
            }
            //关联到动态域字段
            item.setSpecMap(mapSpecPinyin);
        }

        //保存到solr
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        System.out.println("接收的搜索引擎删除指令:"+goodsIds);
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);

        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
