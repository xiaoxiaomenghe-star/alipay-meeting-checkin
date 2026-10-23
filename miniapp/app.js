App({
  globalData: {
    baseUrl: 'http://127.0.0.1:8088',
    token: '',
    demoMode: true,
    demoLocation: { latitude: 31.03, longitude: 121.22 }
  },
  onLaunch() { this.globalData.token = my.getStorageSync({ key: 'token' }).data || ''; }
});
