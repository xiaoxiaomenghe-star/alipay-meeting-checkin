const { request } = require('../../utils/request');

Page({
  data: { token: '', loading: false, error: '', result: null, usedDemoLocation: false },
  onLoad(q) {
    this.setData({ token: decodeURIComponent(q.token || '') });
  },
  locate() {
    return new Promise((resolve, reject) => {
      my.getLocation({
        type: 1,
        success: result => resolve({ latitude: result.latitude, longitude: result.longitude, usedDemoLocation: false }),
        fail: () => {
          const app = getApp();
          if (app.globalData.demoMode) {
            resolve({ ...app.globalData.demoLocation, usedDemoLocation: true });
          } else {
            reject(new Error('无法获取定位，请授权定位后重试'));
          }
        }
      });
    });
  },
  async submit() {
    this.setData({ loading: true, error: '', result: null });
    try {
      const position = await this.locate();
      const result = await request('/api/v1/check-ins', {
        method: 'POST',
        data: {
          entryToken: this.data.token,
          entryType: 'QR',
          latitude: position.latitude,
          longitude: position.longitude
        }
      });
      this.setData({ result, usedDemoLocation: position.usedDemoLocation });
    } catch (e) {
      this.setData({ error: e.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  openRecords() {
    my.navigateTo({ url: '/pages/records/records' });
  }
});
