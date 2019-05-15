<template>
  <v-app>
    <v-toolbar class="primary" style="height: 64px">
      <img src="./assets/logo.png" class="project-logo">
      <v-toolbar-title class="title">After Action Review tool</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn @click.prevent="openDiagram()" class="diagramButton">
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
    </v-toolbar>
    <main>
      <v-layout row wrap>
        <v-flex xs9>
          <records-table style="height: 53vh; overflow: auto;" />
        </v-flex>
        <v-flex xs3>
          <details-panel style="height: 53vh; overflow: auto;" />
        </v-flex>
        <v-flex xs12>
          <timeline-panel style="height: 40vh; overflow: auto;" />
        </v-flex>
      </v-layout>
    </main>
    <diagram-popup/>
  </v-app>
</template>
<script>
  import {eventBus} from './main';
  import EventName from './constants/EventName';
  import {fetchService} from './service/FetchService';

  export default {
    name: 'App',
    data: () => ({
      exportDataItems: [
        { exportType: 'CSV', title: 'CSV' },
        { exportType: 'SQL', title: 'SQL' },
      ]
    }),
    methods: {
      openDiagram () {
        eventBus.$emit(EventName.DIAGRAM_POPUP, true);
      },
      exportData(exportType) {
        console.log("EXPORT DATA", exportType);
        fetchService.performSimpleDownload("exportData?exportType=" + exportType);
      }
    }
  }
</script>
<style lang="stylus">
  @import './stylus/main'
</style>
