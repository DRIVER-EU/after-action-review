<template>
  <v-toolbar class="primary" style="height: 64px">
    <img src="../assets/logo.png" class="project-logo">
    <v-toolbar-title class="title">Monitor and AfterActionReview</v-toolbar-title>
    <v-spacer></v-spacer>
    <slot/>
  </v-toolbar>
</template>

<script>
  export default {
    name: 'Toolbar',
    props: {
    },
  };
</script>
