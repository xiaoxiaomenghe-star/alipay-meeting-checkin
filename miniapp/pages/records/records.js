const { request }=require('../../utils/request');
Page({data:{balance:0,records:[],error:''},onShow(){this.load();},async load(){try{const [points,records]=await Promise.all([request('/api/v1/me/points'),request('/api/v1/me/check-ins')]);this.setData({balance:points.balance,records});}catch(e){this.setData({error:e.message});}}});
