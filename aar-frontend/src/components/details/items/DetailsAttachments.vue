<template>
  <li>
    <v-layout row>
      <v-flex xs4>
        <v-subheader style="align-items: normal;">{{title}}:</v-subheader>
      </v-flex>
      <v-flex xs8>
        <ul class="attachments">
          <li v-for="item in value">
            <a :href="item.name">{{ item.name }}</a>
          </li>
        </ul>
      </v-flex>
    </v-layout>
  </li>
</template>

<script>
  export default {
    name: 'DetailsAttachments',
    props: ['title', 'value']
  };
</script>

<style scoped>

</style>
