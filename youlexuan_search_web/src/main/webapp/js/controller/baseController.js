app.controller('baseController',function ($scope) {
    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }
    $scope.paginationConf={
        currentPage: 1, //当前页
        totalItems: 10, //总记录数
        itemsPerPage: 10,//每页记录数
        perPageOptions: [10, 20, 30, 40, 50],//课选择每页显示的记录数
        //执行分页，会调用方法
        onChange:function () {
            //调用获取服务器端分页数据方法
            $scope.reloadList();
        }

    }

    //要删除数据id的数组
    $scope.selectIds=[];

    //更新复选框
    //$event 事件源对象，可以获取当前复选框的选中状态
    $scope.updateSelection=function ($event,id) {
        if($event.target.checked){
            //如果是选中，把当前复选框对应id存储到要删除数据id的数组
            $scope.selectIds.push(id);
        }else {
            //检索id所在删除数据id的数组所处的角标位置
            var index=	 $scope.selectIds.indexOf(id);
            //如果是取消选中，把当前复选框对应id的值从要删除数据的数组移除
            $scope.selectIds.splice(index,1);
        }
    }

    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔

    $scope.jsonToString=function (jsonString, key) {

        if(jsonString) {
            //转换jsonString为json对象
            var jsonObject = JSON.parse(jsonString);

            //[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            //{"id":27,"text":"网络"}
            //定义一个拼接字符串
            var value = "";
            //遍历json对象数组
            for (var i = 0; i < jsonObject.length; i++) {
                if (i > 0) {
                    value += ",";
                }
                value += jsonObject[i][key];
            }

            return value;
        }


    }

    //从json数组查询指定key 和值的 对象
    $scope.searchObjectByKey=function (list, key, value) {
        //遍历json数组
        for(var i=0;i<list.length;i++){
            //{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}
           if(list[i][key]==value){
               return list[i];
           }
        }

        return null;
    }


})
