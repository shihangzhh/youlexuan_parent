app.service('uploadService',function ($http) {

    //文件上传方法
    this.uploadFile=function () {
       var formData=new FormData();
       //把当前表单里面第一个文件上传选择框里面内容封装
       formData.append("file",file.files[0])
      return  $http({
            method:'post',
            url:'../upload.do',
            data:formData,
            //设置请求头为未定义，这时候，框架就会自动根据传递数据内容识别设定请求头
            //设置为 multipart/form-data
            headers: {'Content-Type':undefined},
            //把传输的数据进行序列化
            transformRequest: angular.identity
        })
    }
})
