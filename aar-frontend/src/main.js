import Vue from 'vue';
import App from './App';
import router from './router';
import Vuetify from 'vuetify';
import {store} from './store';
import 'vuetify/dist/vuetify.min.css';
import VueNativeSock from 'vue-native-websocket';
import axios from 'axios';
import VueAxios from 'vue-axios';
import RecordsTable from './components/RecordsTable';
import DetailsPanel from './components/DetailsPanel';
import TimelinePanel from './components/TimelinePanel';
import JsonTree from './components/JsonTree';
import 'vis/dist/vis.css';
import DiagramPopup from './components/DiagramPopup';
import Urls from './constants/Urls';
import VueLoadImage from 'vue-load-image'


export const eventBus = new Vue();
const host = window.location.host;
const httpUrl = 'http://' + host + '/AARService';
const wsUrl = 'ws://'+ host + '/AARServiceWSEndpoint';

Vue.use(VueAxios, axios.create({
  baseURL: httpUrl
}));
store.axios = Vue.prototype.axios;

Vue.use(VueNativeSock, wsUrl, {
  store: store,
  format: 'json',
  reconnection: true,
  reconnectionDelay: 2000
});
store.$socket = Vue.prototype.$socket;

Vue.use(Vuetify, {
  theme: {
    primary: '#FDB836',
    secondary: '#b0bec5',
    tertiary: '#fff8dc7a',
    accent: '#8c9eff',
    error: '#b71c1c'
  }
});

Vue.config.productionTip = false;

Vue.component('records-table', RecordsTable);
Vue.component('details-panel', DetailsPanel);
Vue.component('timeline-panel', TimelinePanel);
Vue.component('json-tree', JsonTree);
Vue.component('diagram-popup', DiagramPopup);
Vue.component('vue-load-image', VueLoadImage);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App),
  created () {
    this.$store.dispatch('getRecords');
    this.$store.dispatch('getAllTimelineRecords');
    this.$store.dispatch('getActualTrial');
  }
});
