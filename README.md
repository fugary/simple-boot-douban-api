### 简单获取豆瓣BOOK的api

目前使用calibre-web的时候发现豆瓣搜索元数据API已经不可用，自己写一个从网页抓取的API，只实现了简单的查询功能。

### 使用Docker启动

```shell
docker pull fugary/simple-boot-douban-api
docker run -it -p 8085:8085 fugary/simple-boot-douban-api
```
然后可以访问：

http://localhost:8085/v2/book/search?q=深入理解计算机系统

### 群晖calibre-web中使用

下载容器并启动后，需要修改get_meta.js文件，需要进入calibre-web容器中修改。

```shell
vi /calibre-web/app/cps/static/js/get_meta.js
# 找到 var douban = "https://api.douban.com"; 替换成自己的NAS_IP地址
var douban = "http://NAS_IP:8085";
# 如果不熟悉vi命令，这里提供一种更快的替换的方式，使用sed命令：
sed -i 's#https://api.douban.com#http://NAS_IP:8085#g' /calibre-web/app/cps/static/js/get_meta.js
```
参考配置使用文档：https://fugary.com/?p=213
