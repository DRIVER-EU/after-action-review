<template>
  <v-app>
    <v-toolbar class="primary" style="height: 7vh">
      <img src="./assets/logo.png" class="project-logo">
      <v-toolbar-title class="title">After Action Review tool</v-toolbar-title>
      <v-spacer></v-spacer>
    </v-toolbar>
    <main>
      <records-table></records-table>
      <details-panel></details-panel>
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
