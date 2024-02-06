# 烤肠-API分享平台 By Chcwzzz

基于本人的Java SpringBoot 的项目初始模板完成项目的初始构建。

# 本次更新：
1. 完成接口发布和下线功能（仅管理员可操作）
2. 完成新增接口功能
3. 调整数据库设计逻辑，新增RequestParams和ResponseParams对象
4. 修改查询接口信息方法
5. 调整AK、SK秘钥签发规则，修改InterfaceInfo相关对象

# 目前完成的工作有：
1. 用户登录功能
2. 接口信息的CRUD功能
3. 增加模拟接口服务（提供一个POST请求）
4. 实现模拟接口调用功能
5. 通过API签名认证方式保证调用的安全性（AK和SK）
6. 增加客户端SDK方便调用接口（基于SpringBoot Starter）
7. 管理员发布和下线接口功能