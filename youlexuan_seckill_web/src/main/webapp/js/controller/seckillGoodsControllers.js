app.controller('seckillGoodsController',function ($scope,$controller,$interval, $location ,seckillGoodsService) {

    //查找所有参与的秒杀商品
    $scope.findList=function () {

        seckillGoodsService.findList().success(function (response) {
            $scope.list=response;
        })
    }

    //根据商品id查询单个秒杀商品详情信息
    $scope.findOne=function(){
        seckillGoodsService.findOne($location.search()['id']).success(
            function(response){
                $scope.entity= response;
                //计算当前时间距离秒杀结束时间剩余的毫秒
                $scope.allSecond= Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000) ;
                //倒计时
                var time= $interval(function () {
                    if($scope.allSecond>0){
                        $scope.allSecond=$scope.allSecond-1;
                        $scope.times=convertTimeString($scope.allSecond);
                    }else {
                        alert("倒计时结束");
                        $interval.cancel(time);
                    }
                },1000);
            }
        );
    }

    //转化日期格式
    //转换秒数为 格式化后日期
    convertTimeString=function (allSecond) {
        //计算剩余天数
        var days=  Math.floor(allSecond/(60*60*24));
        //计算剩余小时
        var hours=  Math.floor( (allSecond-days*(60*60*24))/(60*60));
        //计算剩余分
        var minutes=  Math.floor( (allSecond-days*(60*60*24)-hours*(60*60))/(60));
        //计算剩余秒数
        var seconds=Math.floor( (allSecond-days*(60*60*24)-hours*(60*60)-minutes*60));
        var str="";
        if(days>0){
            str+=days+"天 ";
        }else {
            str+" ";
        }
        if(hours>0){
            str+=" "+hours+":";
        }
        return str+minutes+":"+seconds;

    }
    //用于调转秒杀详情页面
    $scope.goItem=function (id) {
        location.href="seckill-item.html#?id="+id;
    }

    $scope.submitOrder=function () {

        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {

            if (response.success){
                alert("下单成功，请在三十秒内完成付款");
                location.href='pay.html';
            }else {

                if (response.message=="用户未登录"){

                    location.href='login.html'
                }
                alert(response.message);

            }

        })
    }



})