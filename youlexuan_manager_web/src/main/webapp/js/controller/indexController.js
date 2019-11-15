app.controller('indexController',function ($scope, loginService) {


    //获取当前登录用户名
    $scope.loginName=function () {
        loginService.loginName().success(function (response) {
            $scope.loginName=response.loginName;
        })
    }
})
