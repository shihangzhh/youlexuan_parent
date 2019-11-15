//支付控制层
app.controller('payController',function ($scope,payService,$location) {

    //生成本地二维码
    $scope.createNative=function () {

        payService.createNative().success(function (response) {

            $scope.money=(response.total_fee/100).toFixed(2);//将金额换算成为元
            $scope.out_trade_no= response.out_trade_no;//订单号

         var qr =   new QRious({
                element:document.getElementById('qrious'),
                size:250,
                level:'H',
                value:response.qrcode
            });

         //调用查询支付状态方法

         queryPayStatus(response.out_trade_no);
        });

    }

    //查询支付状态 生成二维码之后就可以查询支付状态
    queryPayStatus=function(out_trade_no){
        payService.queryPayStatus(out_trade_no).success(
            function(response){
                if(response.success){
                    //alert(response.message);
                    location.href="paysuccess.html#?money="+$scope.money;
                }else{
                    if (response.message=="小主，二维码超时了啊") {
                   document.getElementById('timeout').innerText="'二维码\n" +
                       "已过期，刷新页面重新获取二维码。";
                    }else {
                        //alert(response.message);
                        location.href="payfail.html";
                    }

                }
            }
        );
    }

    //使用内置服务对象 将支付钱数传递给支付成功页面
    $scope.getMoney=function () {

        return $location.search()['money'] ;

    }


})