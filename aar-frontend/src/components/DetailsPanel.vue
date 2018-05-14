<template>
  <v-flex xs3>
    <v-card style="height: 93vh">
      <v-card-title class="justify-center primary--text">Details</v-card-title>
      <v-card-text v-if="!recordJson" class="text-xs-center">Choose an entry from the list to show it's details.</v-card-text>
      <v-card-text v-else>
        <log-details v-if="recordType==='Log'" :recordID="recordID" :logJson="recordJson"></log-details>
        <invite-details v-if="recordType==='TopicInvite'" :recordID="recordID" :inviteJson="recordJson"></invite-details>
      </v-card-text>
    </v-card>
  </v-flex>
</template>

<script>
  import {eventBus} from "../main";
  import LogDetails from "./LogDetails";
  import InviteDetails from "./InviteDetails";
    export default {
      components: {LogDetails,InviteDetails},
      name: "DetailsPanel",
      data() {
        return  {
          recordID: '',
          recordType: '',
          recordJson: null
        }
      },
      created() {
        eventBus.$on('recordSelected', (recordID, recordType, recordJson) => {
          this.recordID = recordID
          this.recordType = recordType
          this.recordJson = recordJson
        });
      }
    }
</script>
