app.controller('contentController',function ($scope, contentService) {

    //定义一个数组，存储广告数据
    $scope.contentList=[];
    //获取指定分类的广告数据
    $scope.findByCategoryId=function (categoryId) {
        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId]=response;
        })
    }

    //跳转到搜索程序
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
})
