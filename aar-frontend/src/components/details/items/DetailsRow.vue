<template>
  <li>
    <v-layout row>
      <v-flex xs4>
        <v-subheader style="align-items: normal;height: auto;min-height: 48px;">{{title}}:</v-subheader>
      </v-flex>
      <v-flex xs8>
        {{value}}
      </v-flex>
    </v-layout>
  </li>
</template>

<script>
  export default {
    name: 'DetailsRow',
    props: ['title', 'value']
  };
</script>

<style scoped>

</style>
