app.controller('searchController',function ($scope,$location, searchService) {

    //读取首页传递过来搜索关键字参数
    $scope.loadKeyword=function(){
      var keywords=  $location.search()['keywords'];
      $scope.searchMap.keywords=keywords;

      $scope.search();
    }

    //搜索方法
    $scope.search=function () {
        //转换pageNo为数组
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(function (response) {
          $scope.resultMap=  response;
          //调用分页页码构建方法
            buildPageLabel();

        })
    }
    //定义一个存储搜索条件对象
    $scope.searchMap={"keywords":"","category":"","brand":"",spec:{},'price':'','pageNo':1,'pageSize':20,'sortField':'','sort':''};

    //添加搜索条件
    $scope.addsearchItem=function (key, value) {
      if(key=='category'||key=='brand'||key=='price'){
          $scope.searchMap[key]=value;
      }else {
          $scope.searchMap.spec[key]=value;
      }

      //重置当前页码为1
        $scope.searchMap.pageNo=1;

      //重新查询
        $scope.search();
    }

    //移除搜索条件
    $scope.removesearchItem=function (key) {
        if(key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key]='';
        }else {
            delete $scope.searchMap.spec[key];
        }
        //重置当前页码为1
        $scope.searchMap.pageNo=1;
        //重新查询
        $scope.search();
    }

    //构建分页页码方法
    buildPageLabel=function () {
        //定义数组存储分页数字
        $scope.pageLabel=[];
        //最大页码
        var maxPageNo=$scope.resultMap.totalPages;
        //开始页
        var firstPage=1;
        //结束页
        var lastPage=maxPageNo;
        $scope.firstDot=true;
        $scope.lastDot=true;

        if(maxPageNo>=5) {
            //当前页码 <= 3  只显示 1-5页码
            if ($scope.searchMap.pageNo <= 3) {
                //结束页等于5
                lastPage = 5;
                $scope.firstDot=false;
            } else if ($scope.searchMap.pageNo + 2 >= maxPageNo) {
                //开始页
                firstPage = lastPage - 4;
                //开始要有点
                $scope.lastDot=false;
            } else {
                //开始页
                firstPage = $scope.searchMap.pageNo - 2;
                //结束页
                lastPage = $scope.searchMap.pageNo + 2;

            }
        }else{
             $scope.firstDot=false;
             $scope.lastDot=false;
        }

        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }

    }

    //跳转到指定页码
    $scope.queryByPage=function (pageNo) {

        //判断要跳转到的页码是否 大于0 而且要小于等于总页码
        if(pageNo>0&&pageNo<=$scope.resultMap.totalPages){
            //设置当前页为用户要跳转页码
            $scope.searchMap.pageNo=pageNo;

            //执行跳转
            $scope.search();

        }else {
            console.log('你输入的页码不符合要求:'+pageNo);
        }
    }

    //判断当前页是否是第一页
    $scope.isTop=function () {
      if($scope.searchMap.pageNo==1){
          return true;
      }else {
          return false;
      }
    }
//需要预定义totalPages赋予初始化值
    $scope.resultMap={totalPages:1};
    //判断当前页是否是 最后一页
    $scope.isEnd=function () {
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }

    //判断指定的页码是否等于当前
    $scope.isPage=function (p) {
        if($scope.searchMap.pageNo==parseInt(p)){
            return true;
        }else {
            return false;
        }
    }

    //排序触发事件方法
    $scope.sortSearch=function (sortField, sort) {
         $scope.searchMap.sortField=sortField;
         $scope.searchMap.sort=sort;

         $scope.search();
    }

    //判断搜索关键字里面是否包含当前品牌名称
    $scope.keywordIsBrand=function () {
        //遍历当前分类所属全部品牌集合
       for(var i=0;i<$scope.resultMap.brandList.length;i++){
           if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
               return true;
           }
       }

       return false;
    }
})
