const { request } = require('../../utils/request');

Page({
  data: { meetings: [], loading: false, error: '' },
  onShow() { this.load(); },
  async load() {
    this.setData({ loading: true, error: '' });
    try {
      this.setData({ meetings: await request('/api/v1/meetings') });
    } catch (e) {
      this.setData({ error: e.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  open(e) {
    my.navigateTo({ url: `/pages/meeting/meeting?id=${e.currentTarget.dataset.id}` });
  },
  openRecords() {
    my.navigateTo({ url: '/pages/records/records' });
  }
});
