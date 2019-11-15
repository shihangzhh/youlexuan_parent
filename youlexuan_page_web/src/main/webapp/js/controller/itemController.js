app.controller('itemController',function ($scope,$http) {

    //数量操作
    $scope.addNum=function (num) {
  $scope.num=$scope.num+num;

  if ($scope.num <1){
      $scope.num=1;
  }

    }


    $scope.specificationItems={};//记录用户选择的规格
//用户选择规格
    $scope.selectSpecification=function(name,value){
        $scope.specificationItems[name]=value;

        searchSku();//读取 sku
    }
//判断某规格选项是否被用户选中
    $scope.isSelected=function(name,value){
        if($scope.specificationItems[name]==value){
            return true;
        }else{
            return false;
        }
    }

    //读取sku列表方法
    $scope.loadSku=function () {
        $scope.sku= skuList[0];
        //设置sku规格，设置默认选中规格(深克隆)
        $scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //比对2个json对象，看是否相同
    matchObject=function (json1, json2) {
        //遍历json1，逐个和json2元素比对
        for(var k in json1){
            if(json1[k]!=json2[k]){
                return false;
            }
        }

        //遍历json2，逐个和json1比对
        for(var j in json2){
            if(json2[j]!=json1[j]){
                return false;
            }
        }

        return true;


    }

    //根据用户选中的规格选项比对对应的sku对象
    searchSku=function () {
        //遍历sku集合
        for(var i=0;i<skuList.length;i++){

            //比对每个sku对象里面规格 和 用户选中的规格
            if(matchObject(skuList[i].spec,$scope.specificationItems)){
                $scope.sku=skuList[i];
            }

        }
    }

    //添加购物车方法
    $scope.addtoCart=function () {
 alert("aaa");
        $http.get('http://localhost:9108/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
            function (response) {
                if (response.success){
                    location.href='http://localhost:9108/cart.html';//调转到购物车页面
                }else{

                    alert(response.message);
                }
        });
    }
})