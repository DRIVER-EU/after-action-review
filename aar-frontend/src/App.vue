<template>
  <v-app>
    <v-toolbar class="primary" style="height: 7vh">
      <img src="./assets/logo.png" class="project-logo">
      <v-toolbar-title class="title">After Action Review tool</v-toolbar-title>
      <v-spacer></v-spacer>
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
  </v-app>
</template>
<script>
  export default {
    name: 'App'
  }
</script>
<style lang="stylus">
  @import './stylus/main'
</style>
