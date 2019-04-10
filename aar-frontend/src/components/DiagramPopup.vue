<template>
  <v-layout row justify-center>
    <!--<v-btn color="primary" dark @click.native.stop="dialog = true">Open Dialog</v-btn>-->
    <v-dialog v-model="dialog" max-width="600">
      <v-card class="diagramPopup">
        <v-card-title class="headline">Sequence Diagram</v-card-title>
        <v-card-text>
          <!--<v-img :src="getImageUrl" :lazy-src="require('../assets/sequenceDiagramDummy.png')" class="diagramImage"></v-img>-->
          <vue-load-image class="diagramImage">
            <img slot="image" :src="getImageUrl"/>
            <img slot="preloader" :src="loadingSpinner"/>
            <div slot="error">Missing image.</div>
          </vue-load-image>
        </v-card-text>
        <v-card-actions>
          <v-btn flat="flat" @click="download">Download</v-btn>
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
  import Urls from '../constants/Urls';
  import { saveAs } from 'file-saver';
  import loadingSpinner from '../assets/loading-spinner.gif';

  export default {
    data () {
      return {
        dialog: false,
        loadingSpinner: loadingSpinner,
        t: 0
      };
    },
    computed: {
      getImageUrl: function () {
        if (this.dialog) {
          return Urls.HTTP_BASE + '/createSequenceDiagram?t=' + this.t; // http://localhost:8095/AARService/createSequenceDiagram
        } else {
          return loadingSpinner;
        }
      }
    },
    methods: {
      download: function () {
        const imageUrl = Urls.BASE + '/createSequenceDiagram?t=' + this.t;
        saveAs(imageUrl, "sequenceDiagram.png");
      }
    },
    created () {
      const vm = this;
      eventBus.$on(EventName.DIAGRAM_POPUP, function (value) {
        vm.dialog = value;
        vm.t = new Date().getTime();
      });
    }
  };
</script>
