app.controller('cartController',function ($scope,cartService,addressService) {

    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList=response;
            $scope.totalValue=cartService.sum($scope.cartList);//求合计数

        });
    }

    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(function (response) {

            if (response.success){
                $scope.findCartList();
            }else {
                alert(response.message);
            }

        });
    }

    //根据用户id查询用户的收获地址
    $scope.findListByUserId = function(){

        cartService.findListByUserId().success(function (response) {

            $scope.addressList = response;
            
            //设置默认地址
            for (var i = 0;i < $scope.addressList.length;i++){
                
                if ($scope.addressList[i].isDefault=="1"){

                    $scope.address=$scope.addressList[i];
                }
                
            } 
        })

    }

    //选择地址
    $scope.selectAddress=function (address) {

        $scope.address = address;

    }


    //判断当前地址是否选中
    $scope.isSelectedAddress=function (address) {

        if ($scope.address == address){
            return true;
        } else {

            return false;

        }
    }


    //设置支付方式   在线支付  货到付款

    $scope.order={payType:'1'};

    $scope.selectPayType=function(type){
        $scope.order.payType=type;
    }

    $scope.submitOrder=function () {
        alert("aa");
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人

        cartService.submitOrder($scope.order).success(function (response) {

            if (response.success){

                if ($scope.order.payType=='1'){ //如果是扫码支付的话，则调转到支付页面
                    location.href='pay.html';
                }

            }else{
                alert(response.message);
            }
        });
    }

    //保存
    $scope.save=function(){
        alert("aaa");
        var serviceObject;//服务层对象
        if($scope.entity.id!=null){//如果有ID
            serviceObject=addressService.update( $scope.entity ); //修改
        }else{
            serviceObject=addressService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                   $scope.reloadList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }
    
})