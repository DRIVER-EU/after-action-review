import Vue from 'vue'
import Router from 'vue-router'
import MainPage from '../pages/MainPage';
import SequenceDiagramPage from '../pages/SequenceDiagramPage';
import OverviewSequenceDiagramPage from '../pages/OverviewSequenceDiagramPage';

Vue.use(Router);

export default new Router({
  routes: [
    { path: '/', component: MainPage },
    { path: '/sequenceDiagram', component: SequenceDiagramPage, name: 'sequenceDiagram' },
    { path: '/overviewSequenceDiagram', component: OverviewSequenceDiagramPage, name: 'overviewSequenceDiagram' }
  ]
})
