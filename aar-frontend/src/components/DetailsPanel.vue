<template>
  <v-flex xs3>
    <v-card style="height: 93vh; overflow: auto;">
      <v-card-title class="justify-center primary--text">Details</v-card-title>
      <v-card-text v-if="!recordJson" class="text-xs-center">Choose an entry from the list to show it's details.</v-card-text>
      <v-card-text v-else>
        <log-details v-if="recordType==='Log'" :recordID="recordID" :logJson="recordJson"></log-details>
        <invite-details v-if="recordType==='TopicInvite'" :recordID="recordID" :inviteJson="recordJson"></invite-details>
        <alert-details v-if="recordType==='Alert'" :recordID="recordID" :alertJson="recordJson"></alert-details>
        <geojsonenvelope-details v-if="recordType==='GeoJsonEnvelope'" :recordID="recordID" :envelopeJson="recordJson"></geojsonenvelope-details>
        <largedataupdate-details v-if="recordType==='LargeDataUpdate'" :recordID="recordID" :dataJson="recordJson"></largedataupdate-details>
      </v-card-text>
    </v-card>
  </v-flex>
</template>

<script>
  import {eventBus} from "../main";
  import LogDetails from "./DetailsTemplates/LogDetails";
  import InviteDetails from "./DetailsTemplates/InviteDetails";
  import AlertDetails from "./DetailsTemplates/AlertDetails";
  import GeoJsonEnvelopeDetails from "./DetailsTemplates/GeoJsonEnvelopeDetails";
  import LargeDataUpdateDetails from "./DetailsTemplates/LargeDataUpdateDetails";
    export default {
      components: {LogDetails,InviteDetails,AlertDetails,LargeDataUpdateDetails,GeoJsonEnvelopeDetails},
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
