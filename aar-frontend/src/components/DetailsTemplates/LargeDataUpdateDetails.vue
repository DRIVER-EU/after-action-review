<template>
  <ul style="list-style-type: none;">
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Record ID:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{recordID}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Url:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{recordData.url}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">DataType:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{recordData.dataType}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Title:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{recordData.title}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Description:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{recordData.description}}
        </v-flex>
      </v-layout>
    </li>
  </ul>
</template>

<script>
  export default {
    name: 'LargeDataUpdateDetails',
    props: ['recordID', 'recordData']
  };
</script>

<style scoped>

</style>
