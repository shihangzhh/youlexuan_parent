app.service('contentService',function ($http) {

    //获取指定类目的广告数据
    this.findByCategoryId=function (categoryId) {
     return   $http.get('/content/findByCategoryId.do?categoryId='+categoryId);
    }
})
