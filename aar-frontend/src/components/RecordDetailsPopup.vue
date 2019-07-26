<template>
  <v-layout row justify-center>
    <!--<v-btn color="primary" dark @click.native.stop="dialog = true">Open Dialog</v-btn>-->
    <v-dialog v-model="dialog" max-width="600">
      <v-card class="recordDetailsPopup" style="height: calc(100vh - 160px);overflow: hidden;">
        <v-card-title class="headline">Record Details</v-card-title>
        <v-card-text>
          <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
            <details-panel :hide-title="true"></details-panel>
          </div>
        </v-card-text>
        <v-card-actions style="position:absolute;bottom:0px;height:60px;left:0px;right:0px;">
          <v-spacer></v-spacer>
          <v-btn flat="flat" @click.native="dialog = false">Close</v-btn> <!-- color="green darken-1" -->
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-layout>
</template>
<script>
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';

  export default {
    data () {
      return {
        dialog: false,
      };
    },
    created () {
      const vm = this;
      eventBus.$on(EventName.RECORD_DETAILS_POPUP, function (id) {
        vm.$store.dispatch('getRecord', {id: id});
        vm.dialog = true;
      });
    }
  };
</script>
