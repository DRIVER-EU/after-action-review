<template>
  <li>
    <v-layout row>
      <v-flex xs12>
        <v-subheader style="align-items: normal; height: 24px;">{{title}}:</v-subheader>
      </v-flex>
    </v-layout>
    <v-layout row>
      <v-flex xs12 style="padding-left: 16px;padding-bottom: 32px;">
        <map-panel :geojson="geojson"></map-panel>
      </v-flex>
    </v-layout>
  </li>
</template>

<script>
  export default {
    name: 'DetailsMap',
    props: ['title', 'geojson']
  };
</script>

<style scoped>

</style>
