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
import Toolbar from './components/Toolbar';
import Urls from './constants/Urls';
import VueLoadImage from 'vue-load-image'

export const eventBus = new Vue();
store.eventBus = eventBus;

Vue.use(VueAxios, axios.create({
  baseURL: Urls.HTTP_BASE
}));
store.axios = Vue.prototype.axios;

Vue.use(VueNativeSock, Urls.WEBSOCKET, {
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
Vue.component('json-tree', JsonTree);
Vue.component('diagram-popup', DiagramPopup);
Vue.component('vue-load-image', VueLoadImage);
Vue.component('toolbar', Toolbar);
Vue.component('timeline-panel', TimelinePanel);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
});
