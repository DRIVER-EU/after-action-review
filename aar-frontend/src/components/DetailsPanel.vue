<template>
  <v-flex>
    <v-card v-show="!hideTitle" class="detailsPanel">
      <v-card-title class="justify-space-between primary--text">
        <v-btn icon @click.prevent="toggleWidth">
          <v-icon>{{ this.isWide ? 'keyboard_arrow_right' : 'keyboard_arrow_left' }}</v-icon>
        </v-btn>
        Details
        <div></div>
      </v-card-title>
      <v-card-text v-if="!record.recordData" class="text-xs-center">Choose an entry from the list to show it's details.</v-card-text>
      <v-card-text v-else>
        <component :is="currentComponent" v-bind="currentProperties"></component>
      </v-card-text>
    </v-card>
    <v-flex v-show="hideTitle" class="detailsPanel" style="padding-right:20px;">
      <component :is="currentComponent" v-bind="currentProperties"></component>
    </v-flex>
  </v-flex>
</template>

<script>
  import {eventBus} from '../main';
  import LogDetails from './details/templates/LogDetails';
  import InviteDetails from './details/templates/InviteDetails';
  import AlertDetails from './details/templates/AlertDetails';
  import GeoJsonEnvelopeDetails from './details/templates/GeoJsonEnvelopeDetails';
  import FeatureCollectionDetails from './details/templates/FeatureCollectionDetails';
  import LargeDataUpdateDetails from './details/templates/LargeDataUpdateDetails';
  import FallbackDetails from './details/templates/FallbackDetails';
  import EventName from '../constants/EventName';
  import RecordType from '../constants/RecordType';
  import RolePlayerMessageDetails from './details/templates/RolePlayerMessageDetails';
  import ObserverToolAnswerDetails from './details/templates/ObserverToolAnswerDetails';
  import SessionMgmtDetails from './details/templates/SessionMgmtDetails';
  import PhaseMessageDetails from './details/templates/PhaseMessageDetails';
  import MapLayerUpdateDetails from './details/templates/MapLayerUpdateDetails';

  export default {
    components: {LogDetails, InviteDetails, AlertDetails, LargeDataUpdateDetails, GeoJsonEnvelopeDetails, FeatureCollectionDetails, FallbackDetails, RolePlayerMessageDetails,
      ObserverToolAnswerDetails, SessionMgmtDetails, PhaseMessageDetails, MapLayerUpdateDetails},
    name: 'DetailsPanel',
    props: ['hideTitle', 'onWidthChange'],
    data: function () {
      return {
        isWide: false
      };
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
          case RecordType.FEATURE_COLLECTION:
            return FeatureCollectionDetails.name;
          case RecordType.LARGE_DATA_UPDATE:
            return LargeDataUpdateDetails.name;
          case RecordType.ROLE_PLAYER_MESSAGE:
            return RolePlayerMessageDetails.name;
          case RecordType.OBSERVER_TOOL_ANSWER:
            return ObserverToolAnswerDetails.name;
          case RecordType.SESSION_MGMT:
            return SessionMgmtDetails.name;
          case RecordType.MAP_LAYER_UPDATE:
            return MapLayerUpdateDetails.name;
          default:
            return FallbackDetails.name;
        }
      },
      currentProperties() {
        return { recordID: this.record.id, record: this.record };
      }
    },
    methods: {
      toggleWidth() {
        const newWidth = this.isWide ? 400 : 800;
        this.isWide = !this.isWide;
        this.onWidthChange(newWidth);
      }
    },
    created: function () {
      eventBus.$on(EventName.RECORD_SELECTED, (recordID, recordType) => {
        this.$store.dispatch('getRecord', {id: recordID});
      });
    }
  };
</script>
