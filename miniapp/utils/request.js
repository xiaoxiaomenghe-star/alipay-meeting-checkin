const app = getApp();
function request(path, options = {}) {
  return new Promise((resolve, reject) => {
    my.request({
      url: app.globalData.baseUrl + path,
      method: options.method || 'GET',
      data: options.data,
      headers: { 'Content-Type':'application/json', 'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : '' },
      success: res => res.status >= 200 && res.status < 300 ? resolve(res.data.data) : reject(new Error((res.data && res.data.message) || '请求失败')),
      fail: () => reject(new Error('网络连接失败'))
    });
  });
}
module.exports = { request };
