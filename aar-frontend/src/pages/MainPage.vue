<template>
  <v-app>
    <toolbar>
      <v-btn @click.prevent="openDiagramPage()" class="diagramButton">
        <v-icon left>bar_chart</v-icon>
        Create sequence diagram
      </v-btn>
      <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition">
        <v-btn slot="activator">
          <v-icon left>save_alt</v-icon>
          Export Data
        </v-btn>
        <v-card>
          <v-list>
            <v-list-tile v-for="(item, index) in exportDataItems" :key="index" @click="exportData(item.exportType)">
              <v-list-tile-title v-text="item.title"/>
            </v-list-tile>
          </v-list>
        </v-card>
      </v-menu>
    </toolbar>
    <main style="height: 100%">
      <v-layout column justify-space-between fill-height>
        <div style="position:absolute;top:64px;bottom:300px;left:0px;right:0px;">
          <v-layout row wrap fill-height>
            <div style="position:absolute;top:0px;bottom:0px;left:0px;right:400px;">
              <records-table style="height: 100%; overflow: auto;"/>
            </div>
            <div style="position:absolute;top:0px;bottom:0px;right:0px;width:400px;">
              <details-panel style="height: 100%; overflow: auto;"/>
            </div>
          </v-layout>
        </div>
        <div style="position:absolute;bottom:0px;height:300px;left:0px;right:0px;">
          <timeline-panel style="overflow: auto;"/>
        </div>
      </v-layout>
      <v-snackbar v-model="snackbar.visible" :top="true" :timeout="0" color="error">
        {{snackbar.text}}
        <v-btn flat @click="snackbar.visible = false">
          Close
        </v-btn>
      </v-snackbar>
    </main>
    <diagram-popup/>
  </v-app>
</template>
<script>
  import {recordFilter} from '../service/RecordFilterService';
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import {fetchService} from '../service/FetchService';

  export default {
    name: 'MainPage',
    data: () => ({
      exportDataItems: [
        {exportType: 'CSV', title: 'CSV'},
        {exportType: 'SQL', title: 'SQL'},
      ],
      snackbar: {
        visible: false,
        text: ""
      }
    }),
    methods: {
      openDiagramPopup () {
        eventBus.$emit(EventName.DIAGRAM_POPUP, true);
      },
      openDiagramPage () {
        let routeData = this.$router.resolve({name: 'diagram'});
        window.open(routeData.href, '_blank');
      },
      exportData (exportType) {
        fetchService.performSimpleDownload('exportData?exportType=' + exportType);
      }
    },
    created () {
      const vm = this;
      recordFilter.resetFilter();
      this.$store.dispatch('getPageCount');
      this.$store.dispatch('getFilterOptions');
      this.$store.dispatch('getActualTrial');
      eventBus.$on(EventName.LOG_ERROR_RECEIVED, () => {
        vm.snackbar.text = "A record of type Log on level ERROR has been received.";
        vm.snackbar.visible = true;
      });
    }
  };
</script>