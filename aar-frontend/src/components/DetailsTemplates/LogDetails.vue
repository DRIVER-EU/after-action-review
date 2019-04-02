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
          <v-subheader style="align-items: normal;">Client ID:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{record.recordData.id}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Date/Time:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{record.createDate}} {{record.createTime}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Level:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{record.recordData.level}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Message:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{record.recordData.log}}
        </v-flex>
      </v-layout>
    </li>
  </ul>
</template>

<script>
  export default {
    name: 'LogDetails',
    props: ['recordID', 'record']
  };
</script>

<style scoped>

</style>
