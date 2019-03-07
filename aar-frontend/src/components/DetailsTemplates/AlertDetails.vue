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
          <v-subheader style="align-items: normal;">Sender:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{alertJson.sender}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Date/Time:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{alertJson.sent}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Message:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{alertJson}}
        </v-flex>
      </v-layout>
    </li>
  </ul>
</template>

<script>
    export default {
        name: "Alert",
      props: ['recordID','alertJson']
    }
</script>

<style scoped>

</style>
