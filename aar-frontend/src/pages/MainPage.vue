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
    <main>
      <v-layout row wrap>
        <v-flex xs9>
          <records-table style="height: 53vh; overflow: auto;"/>
        </v-flex>
        <v-flex xs3>
          <details-panel style="height: 53vh; overflow: auto;"/>
        </v-flex>
        <v-flex xs12>
          <timeline-panel style="height: 40vh; overflow: auto;"/>
        </v-flex>
      </v-layout>
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
      ]
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
      recordFilter.resetFilter();
      this.$store.dispatch('getPageCount');
      this.$store.dispatch('getFilterOptions');
      this.$store.dispatch('getActualTrial');
      // this.$store.dispatch('getRecords'); // done implicitly by resetFilter
      // this.$store.dispatch('getAllTimelineRecords'); // done implicitly by resetFilter
    }
  };
</script>
