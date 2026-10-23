const { request } = require('../../utils/request');

Page({
  data: { id: null, detail: null, registered: false, loading: false, error: '' },
  onLoad(q) {
    this.setData({ id: q.id });
    this.load();
  },
  async load() {
    this.setData({ loading: true, error: '' });
    try {
      const [detail, registration] = await Promise.all([
        request(`/api/v1/meetings/${this.data.id}`),
        request(`/api/v1/meetings/${this.data.id}/registration`)
      ]);
      this.setData({ detail, registered: !!registration });
    } catch (e) {
      this.setData({ error: e.message });
    } finally {
      this.setData({ loading: false });
    }
  },
  async register() {
    this.setData({ error: '' });
    try {
      await request(`/api/v1/meetings/${this.data.id}/registrations`, { method: 'POST' });
      this.setData({ registered: true });
      my.showToast({ content: '报名成功' });
    } catch (e) {
      this.setData({ error: e.message });
    }
  },
  checkin() {
    const point = this.data.detail.points[0];
    my.navigateTo({ url: `/pages/checkin/checkin?token=${encodeURIComponent(point.entryToken)}` });
  }
});
