<template>
  <v-flex>
    <v-card class="detailsPanel">
      <v-card-title class="justify-center primary--text">Details</v-card-title>
      <v-card-text v-if="!record.recordData" class="text-xs-center">Choose an entry from the list to show it's details.</v-card-text>
      <v-card-text v-else>
        <component :is="currentComponent" v-bind="currentProperties"></component>
      </v-card-text>
    </v-card>
  </v-flex>
</template>

<script>
  import {eventBus} from '../main';
  import LogDetails from './DetailsTemplates/LogDetails';
  import InviteDetails from './DetailsTemplates/InviteDetails';
  import AlertDetails from './DetailsTemplates/AlertDetails';
  import GeoJsonEnvelopeDetails from './DetailsTemplates/GeoJsonEnvelopeDetails';
  import LargeDataUpdateDetails from './DetailsTemplates/LargeDataUpdateDetails';
  import FallbackDetails from './DetailsTemplates/FallbackDetails';
  import EventName from '../constants/EventName';
  import RecordType from '../constants/RecordType';

  export default {
    components: {LogDetails, InviteDetails, AlertDetails, LargeDataUpdateDetails, GeoJsonEnvelopeDetails, FallbackDetails},
    name: 'DetailsPanel',
    data: function () {
      return {};
    },
    computed: {
      record: function () {
        const record = this.$store.state.record;
        console.log('Displaying details', record);
        return record || {};
      },
      currentComponent() {
        switch (this.record.recordType) {
          case RecordType.LOG:
            return LogDetails.name;
          case RecordType.INVITE:
            return InviteDetails.name;
          case RecordType.ALERT:
            return AlertDetails.name;
          case RecordType.GEO_JSON:
            return GeoJsonEnvelopeDetails.name;
          case RecordType.LARGE_DATA_UPDATE:
            return LargeDataUpdateDetails.name;
          default:
            return FallbackDetails.name;
        }
      },
      currentProperties() {
        switch (this.record.recordType) {
          case RecordType.INVITE:
          case RecordType.LARGE_DATA_UPDATE:
            return { recordID: this.record.id, recordData: this.record.recordData };
          case RecordType.LOG:
          case RecordType.ALERT:
          case RecordType.GEO_JSON:
          default:
            return { recordID: this.record.id, record: this.record };
        }
      }
    },
    created: function () {
      eventBus.$on(EventName.RECORD_SELECTED, (recordID, recordType) => {
        this.$store.dispatch('getRecord', {id: recordID});
      });
    }
  };
</script>
