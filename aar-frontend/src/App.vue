<template>
  <v-app>
    <v-toolbar class="primary" style="height: 64px">
      <img src="./assets/logo.png" class="project-logo">
      <v-toolbar-title class="title">After Action Review tool</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn @click.prevent="openDiagram()" class="diagramButton">Create sequence diagram</v-btn>
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

  export default {
    name: 'App',
    methods: {
      openDiagram () {
        eventBus.$emit(EventName.DIAGRAM_POPUP, true);
      }
    }
  }
</script>
<style lang="stylus">
  @import './stylus/main'
</style>
