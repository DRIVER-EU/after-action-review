<template>
  <v-flex>
    <v-card>
      <v-card-title class="justify-center primary--text">Details</v-card-title>
      <v-card-text v-if="!record.recordJson" class="text-xs-center">Choose an entry from the list to show it's details.</v-card-text>
      <v-card-text v-else>
        <log-details v-if="record.recordType==='Log'" :recordID="record.id" :logJson="record.recordJson"></log-details>
        <invite-details v-if="record.recordType==='TopicInvite'" :recordID="record.id" :inviteJson="record.recordJson"></invite-details>
        <alert-details v-if="record.recordType==='Alert'" :recordID="record.id" :alertJson="record.recordJson"></alert-details>
        <geojsonenvelope-details v-if="record.recordType==='GeoJsonEnvelope'" :recordID="record.id" :envelopeJson="record.recordJson"></geojsonenvelope-details>
        <largedataupdate-details v-if="record.recordType==='LargeDataUpdate'" :recordID="record.id" :dataJson="record.recordJson"></largedataupdate-details>
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
      data: function() {
        return  {
        }
      },
      computed: {
        record: function() {
          const record = this.$store.state.record;
          console.log("Displaying details", record);
          return record || {};
        }
      },
      created: function() {
        eventBus.$on('recordSelected', (recordID, recordType) => {
          this.$store.dispatch('getRecord', {id: recordID});
        });
      }
    }
</script>
