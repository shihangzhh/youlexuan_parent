 //控制层
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){

	$controller('baseController',{$scope:$scope});//继承

    //读取列表数据绑定到表单中
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}

	//分页
	$scope.findPage=function(page,rows){
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	//查询实体
	$scope.findOne=function(){
		var id=$location.search()['id'];
		if(id!=null) {
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    //设置商品介绍（富文本）
					editor.html($scope.entity.goodsDesc.introduction);
					//转换配图json字符串为json对象
                    $scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
                   //转换扩展属性字符串为json对象
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                   //转换读取到的规格字符串为json对象
                    $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                   //遍历sku集合，处理sku对象里面spec转换为json对象
					for(var i=0;i<$scope.entity.itemList.length;i++){
                        $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
					}

                }
            );
        }
	}

	//保存
	$scope.save=function(){
        //从富文本编辑器读取内容，设置
        $scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加
		}
		serviceObject.success(
			function(response){
				if(response.success){
					//保存、更新商品数据成功，跳转到列表页
					location.href="goods.html";

				}else{
					alert(response.message);
				}
			}
		);
	}


	//新增商品数据
	$scope.add=function(){
		//从富文本编辑器读取内容，设置
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(function (response) {
			if(response.success){
              alert("保存商品成功");
             // $scope.entity={};
                $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}
              //清空富文本编辑器
				editor.html('');
			}
        })
	}

	//批量删除
	$scope.dele=function(){
		//获取选中的复选框
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}
			}
		);
	}

	$scope.searchEntity={};//定义搜索对象

	//搜索
	$scope.search=function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	}

	//编写图片上传方法
	$scope.uploadFile=function () {
		uploadService.uploadFile().success(function (response) {
			if(response.success){
				//alert("图片上传成功");
				$scope.image_entity.url=response.message;
			}
        })
    }

    //定义商品json对象数据结构，主要声明其中数组节点
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}

	//添加图片对象保存到itemImages数组
	$scope.add_image_entity=function (image_entity) {
		$scope.entity.goodsDesc.itemImages.push(image_entity);
    }

    //删除图片列表中指定的图片
	$scope.delete_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //读取一级分类列表
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(function (response) {
		$scope.itemCat1List=response;
        })
    }
    //一旦用户选择一级分类，就要去获取该一级分类对应的二级分类
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
         //当newValue别定义有值，category1Id发生了变化
		if(newValue){
			//获取该一级分类对应的二级分类
			itemCatService.findByParentId(newValue).success(function (response) {
				$scope.itemCat2List=response;
            })
		}


    })
    //一旦用户选择二级分类，就要去获取该二级分类对应的三级分类
    $scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
        //当newValue别定义有值，category1Id发生了变化
        if(newValue){
            //获取该一级分类对应的二级分类
            itemCatService.findByParentId(newValue).success(function (response) {
                $scope.itemCat3List=response;
            })
        }


    })

	//根据选择的三级分类id，获取对用模板id
	$scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
		if(newValue){
			//根据选中的三级分类id，获取对应分类信息
			itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.typeTemplateId=	response.typeId;
            })
		}
    })
	//监控模板id，就去获取对应模板信息
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
		if(newValue) {
            //获取对应模板信息
            typeTemplateService.findOne(newValue).success(function (response) {
                $scope.typeTemplate = response;
                //把读取到的品牌json字符串转换成json对象
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                //读取模板对象里面包含的扩展属性
				if($location.search()['id']==null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                }
            })
			//根据模板id，获取对应的规格以及规格选项集合数据
			typeTemplateService.findSpecList(newValue).success(function (response) {
				$scope.specList=response;
            })
        }
    })
	//定义存储选中规格选项数据结构


	//更新选中、取消选中规格选项 name:规格名称  value：规格选项名称
	$scope.updateSpecAttribute=function ($event,name,value) {
     //根据规格key名称(attributeName)、规格名称 搜索集合entity.goodsDesc.specificationItems
	var object=	$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
	if(object==null){
		//首次选中指定规格下的 第一个规格选项
        $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]})
	}else {
		//判断复选框如果是选中
		if($event.target.checked){
			//把勾选中的规格选项插如规格选项数组
			object.attributeValue.push(value);
		}else {
			//取消勾选
			object.attributeValue.splice(object.attributeValue.indexOf(value),1);
			//判断规格选项如果为0，就移除整个规格json对象
			if(object.attributeValue.length==0){
				$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
			}
		}
	}
    }

    //创建sku列表
	$scope.createItemList=function () {
		//定义sku基础结构
		$scope.entity.itemList=[{price:0,num:99999,status:'0',isDefault:'0',spec:{}}];
	var items=	$scope.entity.goodsDesc.specificationItems;
	//遍历用户选中的规格集合
		for(var i=0;i<items.length;i++){
			//扩展sku 规格选项对应列
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    }

    //扩展sku 规格选项对应列 sku列表集合  列的名字（规格名称） 规格选项的值
	addColumn=function (list,columnName,columnValues) {
		//创建一个新集合，装载扩展列成功后sku数据
		var newList=[];
		//遍历sku规格列表
		for(var i=0;i<list.length;i++){
			//读取每行的sku数据
		var oldRow=	list[i];
		//遍历规格选项
			for(var j=0;j<columnValues.length;j++){
				//深克隆
				var newRow=JSON.parse(JSON.stringify(oldRow));
				//在新行扩展列
				newRow.spec[columnName]=columnValues[j];
				//保存新行到新集合
				newList.push(newRow);
			}
		}

		return newList;

    }

    //定义一个数组存储商品审核状态
	$scope.status=['待审核','审核通过','审核未通过','关闭'];

	//定义一个存储全部分类数据 数组
	$scope.itemCatList=[];

	//从后端读取全部分类数据，写入到itemCatList数组
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(function (response) {
			//循环遍历响应的全部的分类集合
			for(var i=0;i<response.length;i++){
             $scope.itemCatList[response[i].id]=response[i].name;
			}
        })
    }

    //根据规格名称和规格选项名称 确认该规格选项是否被选中
	$scope.checkAttributeValue=function (specName, optionName) {
		//规格选项被选中后，存储到了
	var items=$scope.entity.goodsDesc.specificationItems;

	var obj=$scope.searchObjectByKey(items,'attributeName',specName);
   //指定规格名称不存在，表示选项也不存在返回false
	if(obj==null){
       return false;
	}else{
		//指定规格名称存在

        //检查规格选项是否存在
		if(obj.attributeValue.indexOf(optionName)>=0){
			return true;
		}else {
			return false;
		}

	}


    }

});
