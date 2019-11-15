package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    public Map<String,Object> search(Map searchMap);

    //新增指定sku集合，更新到solr搜索引擎
    public void importList(List<TbItem> list);


    //根据商品编号集合，删除指定搜索引擎数据
    public void deleteByGoodsIds(List goodsIds);


}
