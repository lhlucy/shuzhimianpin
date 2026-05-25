// 测试收藏操作的 API 调用
const axios = require('axios');

async function testFavorite() {
  try {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token');
    console.log('token:', token);
    
    if (!token) {
      console.error('未登录，无法测试收藏操作');
      return;
    }
    
    // 测试收藏操作
    const questionId = 1;
    const endpoint = `http://localhost:8080/api/favorites/${questionId}`;
    
    console.log('测试收藏操作...');
    
    // 发送收藏请求
    const response = await axios.post(endpoint, {}, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    console.log('收藏操作成功:', response.data);
    
    // 测试取消收藏操作
    console.log('测试取消收藏操作...');
    
    // 发送取消收藏请求
    const response2 = await axios.delete(endpoint, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    console.log('取消收藏操作成功:', response2.data);
    
  } catch (error) {
    console.error('测试收藏操作失败:', error);
    console.error('错误详情:', error.response);
  }
}

// 运行测试
testFavorite();
