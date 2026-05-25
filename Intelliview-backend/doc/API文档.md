# Intelliview 系统 API 文档

## 项目简介

Intelliview 系统是一个基于 Spring Boot 和 MyBatis Plus 的面试题库系统，提供了用户认证、题目管理、回答管理、评论管理等功能。

## 接口分类

### 1. 认证接口

#### 1.1 发送邮箱验证码
- **接口路径**: `/api/auth/send-code`
- **请求方法**: POST
- **功能描述**: 发送邮箱验证码用于注册、登录或重置密码
- **请求参数**:
  - `email`: 邮箱地址
  - `type`: 验证码类型（register、login、reset_password）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "验证码已发送",
    "data": null
  }
  ```

#### 1.2 邮箱验证码登录
- **接口路径**: `/api/auth/login/email`
- **请求方法**: POST
- **功能描述**: 使用邮箱验证码登录
- **请求参数**:
  - `email`: 邮箱地址
  - `code`: 验证码
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "登录成功",
    "data": {
      "token": "jwt_token",
      "tokenType": "Bearer",
      "expiresIn": 86400000,
      "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "nickname": "测试用户",
        "avatar": "avatar_url",
        "role": "ROLE_USER",
        "emailVerified": true
      }
    }
  }
  ```

#### 1.3 密码登录
- **接口路径**: `/api/auth/login/password`
- **请求方法**: POST
- **功能描述**: 使用用户名和密码登录
- **请求参数**:
  - `username`: 用户名
  - `password`: 密码
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "登录成功",
    "data": {
      "token": "jwt_token",
      "tokenType": "Bearer",
      "expiresIn": 86400000,
      "user": {
        "id": 1,
        "username": "testuser",
        "email": "test@example.com",
        "nickname": "测试用户",
        "avatar": "avatar_url",
        "role": "ROLE_USER",
        "emailVerified": true
      }
    }
  }
  ```

#### 1.4 注册账号
- **接口路径**: `/api/auth/register`
- **请求方法**: POST
- **功能描述**: 注册新用户
- **请求参数**:
  - `username`: 用户名
  - `password`: 密码
  - `email`: 邮箱地址
  - `code`: 邮箱验证码
  - `nickname`: 昵称（可选）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "注册成功",
    "data": {
      "token": "jwt_token",
      "tokenType": "Bearer",
      "expiresIn": 86400000,
      "user": {
        "id": 1,
        "username": "newuser",
        "email": "new@example.com",
        "nickname": "新用户",
        "avatar": "avatar_url",
        "role": "ROLE_USER",
        "emailVerified": true
      }
    }
  }
  ```

#### 1.5 获取GitHub授权URL
- **接口路径**: `/api/auth/github/authorize`
- **请求方法**: GET
- **功能描述**: 获取GitHub授权URL
- **请求参数**:
  - `state`: 状态参数（可选）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "authorizationUrl": "github_auth_url",
      "state": "random_state"
    }
  }
  ```

#### 1.6 GitHub回调
- **接口路径**: `/api/auth/github/callback`
- **请求方法**: POST
- **功能描述**: GitHub回调处理
- **请求参数**:
  - `code`: GitHub授权码
  - `state`: 状态参数
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "GitHub登录成功",
    "data": {
      "token": "jwt_token",
      "tokenType": "Bearer",
      "expiresIn": 86400000,
      "user": {
        "id": 1,
        "username": "github_user",
        "email": "github@example.com",
        "nickname": "GitHub用户",
        "avatar": "github_avatar",
        "role": "ROLE_USER",
        "emailVerified": true
      }
    }
  }
  ```

#### 1.7 退出登录
- **接口路径**: `/api/auth/logout`
- **请求方法**: POST
- **功能描述**: 退出登录
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "退出成功",
    "data": null
  }
  ```

#### 1.8 刷新token
- **接口路径**: `/api/auth/refresh-token`
- **请求方法**: POST
- **功能描述**: 刷新JWT token
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "token刷新成功",
    "data": {
      "token": "new_jwt_token",
      "tokenType": "Bearer",
      "expiresIn": "86400000"
    }
  }
  ```

#### 1.9 重置密码
- **接口路径**: `/api/auth/reset-password`
- **请求方法**: POST
- **功能描述**: 重置密码
- **请求参数**:
  - `email`: 邮箱地址
  - `code`: 验证码
  - `newPassword`: 新密码
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "密码重置成功",
    "data": null
  }
  ```

### 2. 用户接口

#### 2.1 获取当前用户信息
- **接口路径**: `/api/user/profile`
- **请求方法**: GET
- **功能描述**: 获取当前登录用户的详细信息
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "nickname": "测试用户",
      "avatar": "avatar_url",
      "role": "ROLE_USER",
      "emailVerified": true,
      "githubLogin": "github_login",
      "githubAvatar": "github_avatar",
      "githubName": "GitHub Name",
      "githubBio": "GitHub Bio",
      "githubCompany": "GitHub Company",
      "githubBlog": "github_blog",
      "githubLocation": "GitHub Location",
      "lastLoginTime": "2026-02-06T22:11:03.8187173",
      "loginCount": 10,
      "points": 100,
      "level": 5,
      "experience": 500,
      "createTime": "2026-02-06T15:00:15.216509"
    }
  }
  ```

#### 2.2 更新用户信息
- **接口路径**: `/api/user/profile`
- **请求方法**: PUT
- **功能描述**: 更新当前用户的信息
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `nickname`: 昵称
  - `avatar`: 头像URL
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "更新成功",
    "data": {
      "id": 1,
      "username": "testuser",
      "email": "test@example.com",
      "nickname": "新昵称",
      "avatar": "new_avatar_url",
      "role": "ROLE_USER",
      "emailVerified": true,
      "githubLogin": "github_login",
      "githubAvatar": "github_avatar",
      "githubName": "GitHub Name",
      "githubBio": "GitHub Bio",
      "githubCompany": "GitHub Company",
      "githubBlog": "github_blog",
      "githubLocation": "GitHub Location",
      "lastLoginTime": "2026-02-06T22:11:03.8187173",
      "loginCount": 10,
      "points": 100,
      "level": 5,
      "experience": 500,
      "createTime": "2026-02-06T15:00:15.216509"
    }
  }
  ```

#### 2.3 修改密码
- **接口路径**: `/api/user/change-password`
- **请求方法**: POST
- **功能描述**: 修改当前用户的密码
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `oldPassword`: 旧密码
  - `newPassword`: 新密码
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "密码修改成功",
    "data": null
  }
  ```

#### 2.4 发送账户注销验证码
- **接口路径**: `/api/user/send-delete-code`
- **请求方法**: POST
- **功能描述**: 发送账户注销验证码
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "账户注销验证码已发送到您的邮箱",
    "data": null
  }
  ```

#### 2.5 注销账户
- **接口路径**: `/api/user/delete-account`
- **请求方法**: DELETE
- **功能描述**: 注销当前用户的账户
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `code`: 验证码
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "账户已成功注销",
    "data": null
  }
  ```

#### 2.6 获取消息列表
- **接口路径**: `/api/user/messages`
- **请求方法**: GET
- **功能描述**: 获取当前用户的消息列表
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [
      {
        "id": 1,
        "userId": 1,
        "type": "SYSTEM",
        "content": "系统消息内容",
        "isRead": false,
        "createTime": "2026-02-06T22:11:03.8187173"
      }
    ]
  }
  ```

#### 2.7 获取未读消息数
- **接口路径**: `/api/user/messages/unread-count`
- **请求方法**: GET
- **功能描述**: 获取当前用户的未读消息数
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": 5
  }
  ```

#### 2.8 标记消息为已读
- **接口路径**: `/api/user/messages/{messageId}/read`
- **请求方法**: PUT
- **功能描述**: 标记指定消息为已读
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "消息已标记为已读",
    "data": null
  }
  ```

#### 2.9 标记所有消息为已读
- **接口路径**: `/api/user/messages/read-all`
- **请求方法**: PUT
- **功能描述**: 标记所有消息为已读
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "所有消息已标记为已读",
    "data": null
  }
  ```

#### 2.10 删除消息
- **接口路径**: `/api/user/messages/{messageId}`
- **请求方法**: DELETE
- **功能描述**: 删除指定消息
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "消息已删除",
    "data": null
  }
  ```

#### 2.11 清空消息
- **接口路径**: `/api/user/messages`
- **请求方法**: DELETE
- **功能描述**: 清空当前用户的所有消息
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "消息已清空",
    "data": null
  }
  ```

#### 2.12 获取排行榜
- **接口路径**: `/api/user/rankings`
- **请求方法**: GET
- **功能描述**: 获取用户排行榜
- **请求参数**:
  - `type`: 排行类型（默认points）
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "排行榜功能开发中",
    "data": null
  }
  ```

### 3. 题目接口

#### 3.1 获取题目列表（POST版本）
- **接口路径**: `/api/questions/list`
- **请求方法**: POST
- **功能描述**: 获取题目列表，支持筛选、排序、分页
- **请求参数**:
  ```json
  {
    "keyword": "关键词",
    "categoryId": 1,
    "difficulty": "EASY",
    "page": 0,
    "size": 20,
    "sortBy": "createdAt",
    "sortDirection": "desc"
  }
  ```
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "题目标题",
          "slug": "title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 100,
      "size": 20,
      "current": 0,
      "pages": 5
    }
  }
  ```

#### 3.2 获取题目列表（GET版本）
- **接口路径**: `/api/questions/list`
- **请求方法**: GET
- **功能描述**: 获取题目列表，支持筛选、排序、分页
- **请求参数**:
  - `keyword`: 关键词
  - `categoryId`: 分类ID
  - `difficulty`: 难度级别
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
  - `sortBy`: 排序字段（默认createdAt）
  - `sortDirection`: 排序方向（默认desc）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "题目标题",
          "slug": "title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 100,
      "size": 20,
      "current": 0,
      "pages": 5
    }
  }
  ```

#### 3.3 快速获取题目列表
- **接口路径**: `/api/questions`
- **请求方法**: GET
- **功能描述**: 快速获取题目列表
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
  - `keyword`: 关键词
  - `categoryId`: 分类ID
  - `difficulty`: 难度级别
  - `sortBy`: 排序字段（默认createdAt）
  - `sortDirection`: 排序方向（默认desc）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "题目标题",
          "slug": "title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 100,
      "size": 20,
      "current": 0,
      "pages": 5
    }
  }
  ```

#### 3.4 获取题目详情
- **接口路径**: `/api/questions/detail/{id}`
- **请求方法**: GET
- **功能描述**: 获取题目详情
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "id": 1,
      "title": "题目标题",
      "slug": "title-slug",
      "content": "题目内容",
      "solution": "题目解答",
      "category": {
        "id": 1,
        "name": "分类名称"
      },
      "difficulty": "EASY",
      "tags": [
        {
          "id": 1,
          "name": "标签名称"
        }
      ],
      "viewCount": 100,
      "likeCount": 10,
      "answerCount": 5,
      "markCount": 5,
      "browseCount": 100,
      "shareCount": 2,
      "submitCount": 20,
      "acceptCount": 15,
      "acceptRate": 75.0,
      "createdAt": "2026-02-06T15:00:15.216509",
      "updatedAt": "2026-02-06T15:00:15.216509",
      "isLiked": false,
      "isFavorited": false
    }
  }
  ```

#### 3.5 通过slug获取题目详情
- **接口路径**: `/api/questions/slug/{slug}`
- **请求方法**: GET
- **功能描述**: 通过slug获取题目详情
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "id": 1,
      "title": "题目标题",
      "slug": "title-slug",
      "content": "题目内容",
      "solution": "题目解答",
      "category": {
        "id": 1,
        "name": "分类名称"
      },
      "difficulty": "EASY",
      "tags": [
        {
          "id": 1,
          "name": "标签名称"
        }
      ],
      "viewCount": 100,
      "likeCount": 10,
      "answerCount": 5,
      "markCount": 5,
      "browseCount": 100,
      "shareCount": 2,
      "submitCount": 20,
      "acceptCount": 15,
      "acceptRate": 75.0,
      "createdAt": "2026-02-06T15:00:15.216509",
      "updatedAt": "2026-02-06T15:00:15.216509",
      "isLiked": false,
      "isFavorited": false
    }
  }
  ```

#### 3.6 获取相关题目
- **接口路径**: `/api/questions/detail/{id}/related`
- **请求方法**: GET
- **功能描述**: 获取与指定题目相关的题目
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [
      {
        "id": 2,
        "title": "相关题目标题",
        "slug": "related-title-slug",
        "category": {
          "id": 1,
          "name": "分类名称"
        },
        "difficulty": "EASY",
        "tags": [
          {
            "id": 1,
            "name": "标签名称"
          }
        ],
        "viewCount": 50,
        "likeCount": 5,
        "answerCount": 2,
        "createdAt": "2026-02-06T15:00:15.216509"
      }
    ]
  }
  ```

#### 3.7 获取推荐题目
- **接口路径**: `/api/questions/recommended`
- **请求方法**: GET
- **功能描述**: 获取推荐题目
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认10）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "推荐题目标题",
          "slug": "recommended-title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 50,
      "size": 10,
      "current": 0,
      "pages": 5
    }
  }
  ```

#### 3.8 获取热门题目
- **接口路径**: `/api/questions/hot`
- **请求方法**: GET
- **功能描述**: 获取热门题目
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "热门题目标题",
          "slug": "hot-title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 1000,
          "likeCount": 100,
          "answerCount": 50,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 100,
      "size": 20,
      "current": 0,
      "pages": 5
    }
  }
  ```

#### 3.9 根据分类获取题目
- **接口路径**: `/api/questions/category/{categoryId}`
- **请求方法**: GET
- **功能描述**: 根据分类获取题目
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "分类题目标题",
          "slug": "category-title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 50,
      "size": 20,
      "current": 0,
      "pages": 3
    }
  }
  ```

#### 3.10 根据难度获取题目
- **接口路径**: `/api/questions/difficulty/{difficulty}`
- **请求方法**: GET
- **功能描述**: 根据难度获取题目
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "难度题目标题",
          "slug": "difficulty-title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 50,
      "size": 20,
      "current": 0,
      "pages": 3
    }
  }
  ```

#### 3.11 增加题目分享数
- **接口路径**: `/api/questions/detail/{id}/share`
- **请求方法**: POST
- **功能描述**: 增加题目分享数
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "分享成功",
    "data": null
  }
  ```

#### 3.12 增加题目点赞数
- **接口路径**: `/api/questions/detail/{id}/like`
- **请求方法**: POST
- **功能描述**: 增加题目点赞数
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "点赞成功",
    "data": null
  }
  ```

#### 3.13 减少题目点赞数
- **接口路径**: `/api/questions/detail/{id}/unlike`
- **请求方法**: POST
- **功能描述**: 减少题目点赞数
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "取消点赞成功",
    "data": null
  }
  ```

#### 3.14 获取题目统计信息
- **接口路径**: `/api/questions/detail/{id}/stats`
- **请求方法**: GET
- **功能描述**: 获取题目统计信息
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "markCount": 5,
      "browseCount": 100,
      "viewCount": 100,
      "shareCount": 2,
      "submitCount": 20,
      "acceptCount": 15,
      "acceptRate": 75.0
    }
  }
  ```

#### 3.15 获取难度统计
- **接口路径**: `/api/questions/stats/difficulty`
- **请求方法**: GET
- **功能描述**: 获取题目难度统计
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "EASY": 50,
      "MEDIUM": 30,
      "HARD": 20
    }
  }
  ```

#### 3.16 获取用户收藏的题目列表
- **接口路径**: `/api/questions/favorites`
- **请求方法**: GET
- **功能描述**: 获取当前用户收藏的题目列表
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "title": "收藏题目标题",
          "slug": "favorite-title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 10,
      "size": 20,
      "current": 0,
      "pages": 1
    }
  }
  ```

### 4. 回答接口

#### 4.1 创建回答
- **接口路径**: `/api/answers`
- **请求方法**: POST
- **功能描述**: 创建回答
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `questionId`: 题目ID
  - `content`: 回答内容
  - `isAnonymous`: 是否匿名（可选）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "回答创建成功",
    "data": {
      "id": 1,
      "content": "回答内容",
      "user": {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "avatar": "avatar_url"
      },
      "likeCount": 0,
      "commentCount": 0,
      "createdAt": "2026-02-06T15:00:15.216509",
      "updatedAt": "2026-02-06T15:00:15.216509",
      "isLiked": false,
      "isAnonymous": false
    }
  }
  ```

#### 4.2 获取问题的回答列表
- **接口路径**: `/api/answers/question/{questionId}`
- **请求方法**: GET
- **功能描述**: 获取问题的回答列表
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "content": "回答内容",
          "user": {
            "id": 1,
            "username": "testuser",
            "nickname": "测试用户",
            "avatar": "avatar_url"
          },
          "likeCount": 5,
          "commentCount": 2,
          "createdAt": "2026-02-06T15:00:15.216509",
          "updatedAt": "2026-02-06T15:00:15.216509",
          "isLiked": false,
          "isAnonymous": false
        }
      ],
      "total": 10,
      "size": 20,
      "current": 0,
      "pages": 1
    }
  }
  ```

#### 4.3 获取用户的回答列表
- **接口路径**: `/api/answers/user/{userId}`
- **请求方法**: GET
- **功能描述**: 获取指定用户的回答列表
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "content": "回答内容",
          "question": {
            "id": 1,
            "title": "题目标题",
            "slug": "title-slug"
          },
          "likeCount": 5,
          "commentCount": 2,
          "createdAt": "2026-02-06T15:00:15.216509",
          "updatedAt": "2026-02-06T15:00:15.216509",
          "isLiked": false
        }
      ],
      "total": 5,
      "size": 20,
      "current": 0,
      "pages": 1
    }
  }
  ```

#### 4.4 获取回答详情
- **接口路径**: `/api/answers/{answerId}`
- **请求方法**: GET
- **功能描述**: 获取回答详情
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "id": 1,
      "content": "回答内容",
      "user": {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "avatar": "avatar_url"
      },
      "question": {
        "id": 1,
        "title": "题目标题",
        "slug": "title-slug"
      },
      "likeCount": 5,
      "commentCount": 2,
      "createdAt": "2026-02-06T15:00:15.216509",
      "updatedAt": "2026-02-06T15:00:15.216509",
      "isLiked": false,
      "isAnonymous": false
    }
  }
  ```

#### 4.5 更新回答
- **接口路径**: `/api/answers/{answerId}`
- **请求方法**: PUT
- **功能描述**: 更新回答
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `content`: 回答内容
  - `isAnonymous`: 是否匿名（可选）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "回答更新成功",
    "data": {
      "id": 1,
      "content": "更新后的回答内容",
      "user": {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "avatar": "avatar_url"
      },
      "likeCount": 5,
      "commentCount": 2,
      "createdAt": "2026-02-06T15:00:15.216509",
      "updatedAt": "2026-02-06T16:00:15.216509",
      "isLiked": false,
      "isAnonymous": false
    }
  }
  ```

#### 4.6 删除回答
- **接口路径**: `/api/answers/{answerId}`
- **请求方法**: DELETE
- **功能描述**: 删除回答
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "回答删除成功",
    "data": null
  }
  ```

#### 4.7 获取问题的热门回答
- **接口路径**: `/api/answers/question/{questionId}/top`
- **请求方法**: GET
- **功能描述**: 获取问题的热门回答
- **请求参数**:
  - `limit`: 限制数量（默认5）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [
      {
        "id": 1,
        "content": "热门回答内容",
        "user": {
          "id": 1,
          "username": "testuser",
          "nickname": "测试用户",
          "avatar": "avatar_url"
        },
        "likeCount": 10,
        "commentCount": 3,
        "createdAt": "2026-02-06T15:00:15.216509",
        "updatedAt": "2026-02-06T15:00:15.216509",
        "isLiked": false,
        "isAnonymous": false
      }
    ]
  }
  ```

## 5. 其他接口

### 5.1 搜索接口
- **接口路径**: `/api/search`
- **请求方法**: POST
- **功能描述**: 搜索题目、用户等
- **请求参数**:
  ```json
  {
    "keyword": "关键词",
    "type": "QUESTION",
    "page": 0,
    "size": 20
  }
  ```
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "questions": [
        {
          "id": 1,
          "title": "搜索结果题目",
          "slug": "search-result-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "users": [],
      "total": 1
    }
  }
  ```

### 5.2 评论接口
- **接口路径**: `/api/comments`
- **请求方法**: POST
- **功能描述**: 创建评论
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `targetId`: 目标ID（问题或回答ID）
  - `targetType`: 目标类型（QUESTION或ANSWER）
  - `content`: 评论内容
  - `isAnonymous`: 是否匿名（可选）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "评论创建成功",
    "data": {
      "id": 1,
      "content": "评论内容",
      "user": {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "avatar": "avatar_url"
      },
      "likeCount": 0,
      "createdAt": "2026-02-06T15:00:15.216509",
      "updatedAt": "2026-02-06T15:00:15.216509",
      "isLiked": false,
      "isAnonymous": false
    }
  }
  ```

### 5.3 点赞接口

#### 5.3.1 点赞题目
- **接口路径**: `/api/likes/question/{questionId}`
- **请求方法**: POST
- **功能描述**: 点赞题目
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "点赞成功",
    "data": null
  }
  ```

#### 5.3.2 取消点赞题目
- **接口路径**: `/api/likes/question/{questionId}`
- **请求方法**: DELETE
- **功能描述**: 取消点赞题目
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "取消点赞成功",
    "data": null
  }
  ```

#### 5.3.3 点赞回答
- **接口路径**: `/api/likes/answer/{answerId}`
- **请求方法**: POST
- **功能描述**: 点赞回答
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "点赞成功",
    "data": null
  }
  ```

#### 5.3.4 取消点赞回答
- **接口路径**: `/api/likes/answer/{answerId}`
- **请求方法**: DELETE
- **功能描述**: 取消点赞回答
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "取消点赞成功",
    "data": null
  }
  ```

#### 5.3.5 点赞评论
- **接口路径**: `/api/likes/comment/{commentId}`
- **请求方法**: POST
- **功能描述**: 点赞评论
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "点赞成功",
    "data": null
  }
  ```

#### 5.3.6 取消点赞评论
- **接口路径**: `/api/likes/comment/{commentId}`
- **请求方法**: DELETE
- **功能描述**: 取消点赞评论
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "取消点赞成功",
    "data": null
  }
  ```

#### 5.3.7 检查是否点赞
- **接口路径**: `/api/likes/check`
- **请求方法**: GET
- **功能描述**: 检查用户是否已点赞目标
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `targetType`: 目标类型（QUESTION、ANSWER或COMMENT）
  - `targetId`: 目标ID
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": true
  }
  ```

#### 5.3.8 获取点赞数
- **接口路径**: `/api/likes/count`
- **请求方法**: GET
- **功能描述**: 获取目标的点赞数
- **请求参数**:
  - `targetType`: 目标类型（QUESTION、ANSWER或COMMENT）
  - `targetId`: 目标ID
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": 6
  }
  ```

### 5.4 收藏接口

#### 5.4.1 收藏题目
- **接口路径**: `/api/favorites/{questionId}`
- **请求方法**: POST
- **功能描述**: 收藏指定题目
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `questionId`: 题目ID（路径参数）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "收藏成功",
    "data": null
  }
  ```

#### 5.4.2 取消收藏题目
- **接口路径**: `/api/favorites/{questionId}`
- **请求方法**: DELETE
- **功能描述**: 取消收藏指定题目
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `questionId`: 题目ID（路径参数）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "取消收藏成功",
    "data": null
  }
  ```

#### 5.4.3 获取用户的收藏列表
- **接口路径**: `/api/favorites`
- **请求方法**: GET
- **功能描述**: 获取当前用户的收藏题目列表，支持分页
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "total": 5,
      "records": [
        {
          "id": 1,
          "title": "题目标题",
          "slug": "title-slug",
          "category": {
            "id": 1,
            "name": "分类名称"
          },
          "difficulty": "EASY",
          "tags": [
            {
              "id": 1,
              "name": "标签名称"
            }
          ],
          "viewCount": 100,
          "likeCount": 10,
          "answerCount": 5,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "current": 0,
      "size": 20,
      "pages": 1
    }
  }
  ```

#### 5.4.4 检查题目是否已收藏
- **接口路径**: `/api/favorites/{questionId}/check`
- **请求方法**: GET
- **功能描述**: 检查当前用户是否已收藏指定题目
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `questionId`: 题目ID（路径参数）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": true
  }
  ```

#### 5.4.5 获取用户的收藏数量
- **接口路径**: `/api/favorites/count`
- **请求方法**: GET
- **功能描述**: 获取当前用户的收藏题目数量
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": 5
  }
  ```

### 5.5 举报接口
- **接口路径**: `/api/reports`
- **请求方法**: POST
- **功能描述**: 举报
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `targetId`: 目标ID（问题、回答或评论ID）
  - `targetType`: 目标类型（QUESTION、ANSWER或COMMENT）
  - `reason`: 举报原因
  - `description`: 详细描述（可选）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "举报成功",
    "data": null
  }
  ```

### 5.6 AI面试接口
- **接口路径**: `/api/ai-interview/questions`
- **请求方法**: POST
- **功能描述**: 获取AI面试题目
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `jobType`: 职位类型
  - `difficulty`: 难度级别
  - `count`: 题目数量
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "获取AI面试题目成功",
    "data": [
      {
        "id": 1,
        "question": "AI面试题目",
        "difficulty": "EASY",
        "category": "技术"
      }
    ]
  }
  ```

## 6. 管理员接口

### 6.1 题目管理接口

#### 6.1.1 创建题目（v2版本，支持完整功能）
- **接口路径**: `/api/admin/questions/v2`
- **请求方法**: POST
- **功能描述**: 创建题目，支持完整功能（包含标签、关联题目、元数据等）
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  ```json
  {
    "title": "题目标题",
    "description": "题目描述",
    "content": "题目内容",
    "solution": "题目解答",
    "difficulty": "EASY",
    "categoryId": 1,
    "isVisible": true,
    "sortOrder": 1,
    "tags": ["标签1", "标签2"],
    "keyPoints": [
      {
        "content": "关键点1",
        "sortOrder": 1
      }
    ],
    "relatedQuestionIds": [2, 3],
    "metadata": {
      "source": "网络",
      "reference": "https://example.com"
    }
  }
  ```
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "创建成功",
    "data": {
      "id": 1,
      "title": "题目标题",
      "slug": "title-slug",
      "description": "题目描述",
      "content": "题目内容",
      "solution": "题目解答",
      "difficulty": "EASY",
      "difficultyLabel": "简单",
      "categoryId": 1,
      "categoryName": "分类名称",
      "tags": [
        {
          "id": 1,
          "name": "标签1",
          "slug": "tag-1"
        }
      ],
      "keyPoints": [
        {
          "content": "关键点1",
          "sortOrder": 1
        }
      ],
      "relatedQuestions": [
        {
          "id": 2,
          "title": "相关题目标题",
          "slug": "related-title"
        }
      ],
      "isVisible": true,
      "sortOrder": 1,
      "createdAt": "2026-02-07T10:00:00.000Z",
      "updatedAt": "2026-02-07T10:00:00.000Z"
    }
  }
  ```

#### 6.1.2 更新题目（v2版本，支持完整功能）
- **接口路径**: `/api/admin/questions/v2/{id}`
- **请求方法**: PUT
- **功能描述**: 更新题目，支持完整功能
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**: 与创建题目相同
- **响应格式**: 与创建题目相同

#### 6.1.3 获取题目详情（v2版本，包含完整信息）
- **接口路径**: `/api/admin/questions/v2/{id}`
- **请求方法**: GET
- **功能描述**: 获取题目详情，包含完整信息
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**: 与创建题目相同

#### 6.1.4 搜索题目（用于相关题目选择）
- **接口路径**: `/api/admin/questions/search/suggest`
- **请求方法**: GET
- **功能描述**: 搜索题目，用于相关题目选择
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `keyword`: 关键词
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [
      {
        "id": 1,
        "title": "题目标题",
        "slug": "title-slug",
        "difficulty": "EASY",
        "difficultyLabel": "简单"
      }
    ]
  }
  ```

#### 6.1.5 获取标签建议
- **接口路径**: `/api/admin/questions/tags/suggest`
- **请求方法**: GET
- **功能描述**: 获取标签建议
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `keyword`: 关键词
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [
      {
        "id": 1,
        "name": "标签1",
        "slug": "tag-1"
      }
    ]
  }
  ```

#### 6.1.6 批量导入题目（Excel格式）
- **接口路径**: `/api/admin/questions/import/excel`
- **请求方法**: POST
- **功能描述**: 批量导入题目（Excel格式）
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `file`: Excel文件
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "导入成功",
    "data": [
      {
        "id": 1,
        "title": "题目标题",
        "slug": "title-slug"
      }
    ]
  }
  ```

#### 6.1.7 批量导入题目（JSON格式）
- **接口路径**: `/api/admin/questions/import/json`
- **请求方法**: POST
- **功能描述**: 批量导入题目（JSON格式）
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `file`: JSON文件
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "导入成功",
    "data": [
      {
        "id": 1,
        "title": "题目标题",
        "slug": "title-slug"
      }
    ]
  }
  ```

#### 6.1.8 导出题目（Excel格式）
- **接口路径**: `/api/admin/questions/export/excel`
- **请求方法**: GET
- **功能描述**: 导出题目（Excel格式）
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `keyword`: 关键词（可选）
  - `categoryId`: 分类ID（可选）
  - `difficulty`: 难度级别（可选）
  - `questionIds`: 题目ID列表（可选）
  - `exportAll`: 是否导出所有题目（默认false）
- **响应格式**: Excel文件下载

#### 6.1.9 创建题目（旧版本，保持兼容）
- **接口路径**: `/api/admin/questions`
- **请求方法**: POST
- **功能描述**: 创建题目（旧版本，功能有限）
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  ```json
  {
    "title": "题目标题",
    "content": "题目内容",
    "categoryId": 1,
    "difficulty": "EASY",
    "tags": [1, 2, 3]
  }
  ```
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "创建成功",
    "data": {
      "id": 1,
      "title": "题目标题",
      "slug": "title-slug"
    }
  }
  ```

### 6.2 分类管理接口
- **接口路径**: `/api/admin/categories`
- **请求方法**: POST
- **功能描述**: 创建分类
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  ```json
  {
    "name": "分类名称",
    "description": "分类描述"
  }
  ```
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "分类创建成功",
    "data": {
      "id": 1,
      "name": "分类名称"
    }
  }
  ```

### 6.3 标签管理接口
- **接口路径**: `/api/admin/tags`
- **请求方法**: POST
- **功能描述**: 创建标签
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  ```json
  {
    "name": "标签名称",
    "description": "标签描述"
  }
  ```
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "标签创建成功",
    "data": {
      "id": 1,
      "name": "标签名称"
    }
  }
  ```

### 6.4 用户管理接口
- **接口路径**: `/api/admin/users`
- **请求方法**: GET
- **功能描述**: 获取用户列表
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `page`: 页码（默认0）
  - `size`: 每页大小（默认20）
  - `username`: 用户名（可选）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "records": [
        {
          "id": 1,
          "username": "testuser",
          "email": "test@example.com",
          "nickname": "测试用户",
          "role": "ROLE_USER",
          "enabled": true,
          "createdAt": "2026-02-06T15:00:15.216509"
        }
      ],
      "total": 100,
      "size": 20,
      "current": 0,
      "pages": 5
    }
  }
  ```

### 6.5 仪表盘接口

#### 6.5.1 获取系统概览统计数据
- **接口路径**: `/api/admin/dashboard/stats`
- **请求方法**: GET
- **功能描述**: 获取系统概览统计数据，包括题目总数、试卷总数、题库总数、用户总数
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "questionCount": 1250,
      "paperCount": 56,
      "bankCount": 28,
      "userCount": 156
    }
  }
  ```

#### 6.5.2 获取最近活动日志
- **接口路径**: `/api/admin/dashboard/activities`
- **请求方法**: GET
- **功能描述**: 获取系统最近的活动日志
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [
      {
        "title": "用户管理",
        "description": "创建了新用户 testuser",
        "user": "admin",
        "time": "2026-02-08T15:30:00"
      },
      {
        "title": "题目管理",
        "description": "更新了题目 'Java多线程原理'",
        "user": "admin",
        "time": "2026-02-08T14:20:00"
      }
    ]
  }
  ```

#### 6.5.3 获取题目难度分布
- **接口路径**: `/api/admin/dashboard/stats/questions/difficulty`
- **请求方法**: GET
- **功能描述**: 获取题目难度分布数据
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "EASY": 45,
      "MEDIUM": 30,
      "HARD": 25
    }
  }
  ```

#### 6.5.4 获取题目类型分布
- **接口路径**: `/api/admin/dashboard/stats/questions/type`
- **请求方法**: GET
- **功能描述**: 获取题目类型分布数据
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "Java": 30,
      "Python": 20,
      "前端": 15,
      "算法": 15,
      "数据库": 10,
      "其他": 10
    }
  }
  ```

### 6.6 批量上传接口

#### 6.6.1 批量上传题目
- **接口路径**: `/api/admin/questions/batch/upload`
- **请求方法**: POST
- **功能描述**: 批量上传题目，支持Excel和CSV文件
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `file`: 上传文件（multipart/form-data）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": "上传成功",
    "data": [
      {
        "id": 1,
        "title": "Java多线程原理",
        "slug": "java-multithreading-principle"
      }
    ]
  }
  ```

#### 6.6.2 下载上传模板
- **接口路径**: `/api/admin/questions/batch/template`
- **请求方法**: GET
- **功能描述**: 下载批量上传题目的模板文件
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**: Excel文件下载

## 7. 响应格式说明

所有API接口的响应格式统一为：

```json
{
  "success": true,  // 是否成功
  "message": "消息",  // 响应消息
  "data": {}  // 响应数据
}
```

## 8. 题库相关接口

### 8.1 获取题库列表
- **接口路径**: `/api/banks/list`
- **请求方法**: GET
- **功能描述**: 获取题库列表，支持分页和排序
- **请求参数**:
  - `page`: 页码，默认0
  - `size`: 每页条数，默认10
  - `sortBy`: 排序字段，默认createdAt
  - `sortDirection`: 排序方向，默认desc
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "total": 10,
      "records": [
        {
          "id": 1,
          "title": "Java核心面试题",
          "description": "Java核心知识点面试题汇总",
          "icon": "el-icon-document",
          "questionCount": 50,
          "popular": true,
          "viewCount": 1000
        }
      ],
      "current": 0,
      "size": 10,
      "pages": 1
    }
  }
  ```

### 8.2 获取题库详情
- **接口路径**: `/api/banks/detail`
- **请求方法**: GET
- **功能描述**: 获取题库详情
- **请求参数**:
  - `id`: 题库ID
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "id": 1,
      "title": "Java核心面试题",
      "description": "Java核心知识点面试题汇总",
      "icon": "el-icon-document",
      "questionCount": 50,
      "popular": true,
      "viewCount": 1000
    }
  }
  ```

### 8.3 获取题库题目列表
- **接口路径**: `/api/banks/{bankId}/questions`
- **请求方法**: GET
- **功能描述**: 获取指定题库中的题目列表，支持分页
- **请求参数**:
  - `bankId`: 题库ID（路径参数）
  - `page`: 页码，默认0
  - `size`: 每页条数，默认10
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "total": 20,
      "records": [
        {
          "id": 1,
          "title": "Java方法重载和方法重写之间的区别是什么?",
          "slug": "method-overloading-vs-overriding",
          "description": "详细说明Java中方法重载和方法重写的区别",
          "difficulty": "MEDIUM",
          "difficultyLabel": "中等",
          "categoryId": 7,
          "markCount": 10,
          "shareCount": 5,
          "browseCount": 100,
          "viewCount": 80
        }
      ],
      "current": 0,
      "size": 10,
      "pages": 2
    }
  }
  ```

## 9. 用户练习历史接口

### 9.1 记录用户刷题行为
- **接口路径**: `/api/practice-history/record`
- **请求方法**: POST
- **功能描述**: 记录用户的刷题行为，包括是否完成、是否查看答案、用时等
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  ```json
  {
    "questionId": 1,
    "completed": true,
    "viewAnswer": false,
    "duration": 300
  }
  ```
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "id": 1,
      "userId": 1,
      "questionId": 1,
      "completed": true,
      "viewAnswer": false,
      "duration": 300,
      "createdAt": "2026-02-07T22:37:20",
      "updatedAt": "2026-02-07T22:37:20"
    }
  }
  ```

### 9.2 获取用户的刷题记录列表
- **接口路径**: `/api/practice-history/list`
- **请求方法**: GET
- **功能描述**: 获取当前用户的刷题记录列表，支持分页
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `page`: 页码，默认0
  - `size`: 每页大小，默认10
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [
      {
        "id": 1,
        "questionId": 1,
        "questionTitle": "Java方法重载和方法重写之间的区别是什么?",
        "questionSlug": "method-overloading-vs-overriding",
        "difficulty": "MEDIUM",
        "difficultyLabel": "中等",
        "completed": true,
        "viewAnswer": false,
        "duration": 300,
        "createdAt": "2026-02-07T22:37:20",
        "updatedAt": "2026-02-07T22:37:20"
      }
    ]
  }
  ```

### 9.3 获取用户的刷题统计信息
- **接口路径**: `/api/practice-history/stats`
- **请求方法**: GET
- **功能描述**: 获取当前用户的刷题统计信息，包括总刷题数、已完成题目数、查看答案的题目数、总刷题时长等
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "totalCount": 10,
      "completedCount": 8,
      "viewAnswerCount": 2,
      "totalDuration": 50,
      "completionRate": 80.0
    }
  }
  ```

### 9.4 检查题目是否已完成
- **接口路径**: `/api/practice-history/completed/{questionId}`
- **请求方法**: GET
- **功能描述**: 检查当前用户是否已完成指定题目
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **请求参数**:
  - `questionId`: 题目ID（路径参数）
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": true
  }
  ```

### 9.5 获取用户已完成的题目ID列表
- **接口路径**: `/api/practice-history/completed-questions`
- **请求方法**: GET
- **功能描述**: 获取当前用户已完成的所有题目ID列表
- **请求头**:
  - `Authorization`: Bearer jwt_token
- **响应格式**:
  ```json
  {
    "success": true,
    "message": null,
    "data": [1, 2, 3, 4, 5]
  }
  ```

## 10. 认证说明

### 10.1 JWT认证
- 所有需要认证的接口都需要在请求头中携带JWT token
- 请求头格式：`Authorization: Bearer jwt_token`
- token有效期为24小时

### 10.2 权限说明
- `ROLE_USER`: 普通用户权限，可以访问大部分接口
- `ROLE_ADMIN`: 管理员权限，可以访问所有接口，包括管理员专属接口
- `ROLE_TEACHER`: 教师权限，介于普通用户和管理员之间

## 11. 错误处理

当API请求失败时，会返回错误信息：

```json
{
  "success": false,
  "message": "错误消息",
  "data": null
}
```

常见错误码：
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器内部错误

## 12. 测试账号

### 12.1 管理员账号
- 用户名: admin
- 密码: admin123
- 邮箱: admin@interview-duck.com

### 12.2 普通用户账号
- 用户名: testuser
- 密码: test123
- 邮箱: test@interview-duck.com

## 13. 接口调用示例

### 13.1 使用curl调用登录接口

```bash
curl -X POST http://localhost:8080/api/auth/login/password \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "test123"}'
```

### 13.2 使用curl调用需要认证的接口

```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer jwt_token"
```

### 13.3 使用JavaScript调用接口

```javascript
// 登录
fetch('http://localhost:8080/api/auth/login/password', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ username: 'testuser', password: 'test123' })
})
.then(response => response.json())
.then(data => {
  const token = data.data.token;
  
  // 使用token调用其他接口
  fetch('http://localhost:8080/api/user/profile', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  .then(response => response.json())
  .then(userData => {
    console.log('用户信息:', userData.data);
  });
});
```

## 14. 注意事项

1. 所有接口都支持CORS跨域请求
2. 接口返回的数据结构可能会根据版本更新而变化，请以实际返回为准
3. 接口调用频率限制：每IP每分钟最多调用60次
4. 上传文件大小限制：单个文件最大10MB
5. 请在生产环境中使用HTTPS协议

---

**文档生成时间**: 2026-02-07
**版本**: 1.1.3
