<template>
  <li>
    <v-layout row>
      <v-flex xs12>
        <v-subheader style="align-items: normal; height: 24px;">{{title}}:</v-subheader>
      </v-flex>
    </v-layout>
    <v-layout row>
      <v-flex xs12>
        <json-tree :json="value"></json-tree>
      </v-flex>
    </v-layout>
  </li>
</template>

<script>
  export default {
    name: 'DetailsJsonTree',
    props: ['title', 'value']
  };
</script>

<style scoped>

</style>
