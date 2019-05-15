import Vue from 'vue'
import Router from 'vue-router'
import MainPage from '../pages/MainPage';
import DiagramPage from '../pages/DiagramPage';

Vue.use(Router);

export default new Router({
  routes: [
    { path: '/', component: MainPage },
    { path: '/diagram', component: DiagramPage, name: 'diagram' }
  ]
})
