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
          <v-subheader style="align-items: normal;">ID:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{inviteJson.id}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Topic name:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{inviteJson.topicName}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Subscribing allowed:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{inviteJson.subscribeAllowed}}
        </v-flex>
      </v-layout>
    </li>
    <li>
      <v-layout row>
        <v-flex xs4>
          <v-subheader style="align-items: normal;">Publishing allowed:</v-subheader>
        </v-flex>
        <v-flex xs8>
          {{inviteJson.publishAllowed}}
        </v-flex>
      </v-layout>
    </li>
  </ul>
</template>

<script>
  export default {
    name: "InviteDetails",
    props: ['recordID','inviteJson']
  }
</script>

<style scoped>

</style>
