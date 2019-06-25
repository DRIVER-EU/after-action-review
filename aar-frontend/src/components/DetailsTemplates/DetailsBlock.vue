<template>
  <li>
    <v-layout row>
      <v-flex xs12>
        <v-subheader style="align-items: normal;height: 24px;">{{title}}:</v-subheader>
      </v-flex>
    </v-layout>
    <v-layout row>
      <v-flex xs12 style="padding-left: 16px;">
        {{value}}
      </v-flex>
    </v-layout>
  </li>
</template>

<script>
  export default {
    name: 'DetailsBlock',
    props: ['title', 'value']
  };
</script>

<style scoped>

</style>
