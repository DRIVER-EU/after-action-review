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
          {{dataJson.url}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">DataType:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{dataJson.dataType}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Title:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{dataJson.title}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Description:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{dataJson.description}}
        </v-flex>
      </v-layout>
    </li>
  </ul>
</template>

<script>
    export default {
        name: "LargeDataUpdate",
      props: ['recordID','dataJson']
    }
</script>

<style scoped>

</style>
