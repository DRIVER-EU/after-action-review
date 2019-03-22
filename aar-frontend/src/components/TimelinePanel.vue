<template>
  <v-flex>
    <v-card>
      <v-card-title class="justify-center primary--text">Timeline</v-card-title>
      <v-card-text v-if="!recordData" class="text-xs-center">Choose an entry from the list to show it's timeline.</v-card-text>
      <v-card-text v-else>
        hello timeline!
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
      name: "TimelinePanel",
      data() {
        return  {
          recordID: '',
          recordData: null
        }
      },
      created() {
        eventBus.$on('recordSelected', (recordID, recordData) => {
        });
      }
    }
</script>
