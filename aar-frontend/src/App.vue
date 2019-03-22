<template>
  <v-app>
    <v-toolbar class="primary" style="height: 7vh">
      <img src="./assets/logo.png" class="project-logo">
      <v-toolbar-title class="title">After Action Review tool</v-toolbar-title>
      <v-spacer></v-spacer>
    </v-toolbar>
    <main>
      <v-flex xs9>
          <v-flex>
            <records-table style="height: 73vh; overflow: auto;" />
          </v-flex>
          <v-flex>
            <timeline-panel style="height: 20vh; overflow: auto;" />
          </v-flex>
      </v-flex>
      <v-flex xs3>
        <details-panel style="height: 93vh; overflow: auto;" />
      </v-flex>
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
