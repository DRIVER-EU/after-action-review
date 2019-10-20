<template>
  <v-layout row justify-center>
    <!--<v-btn color="primary" dark @click.native.stop="dialog = true">Open Dialog</v-btn>-->
    <v-dialog v-model="dialog" max-width="600">
      <v-card class="attachmentPopup">
        <v-card-title class="headline">{{fileName}}</v-card-title>
        <v-card-text>
          <!--<v-img :src="getImageUrl" :lazy-src="require('../assets/sequenceDiagramDummy.png')" class="diagramImage"></v-img>-->
          <vue-load-image class="attachmentImage">
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
        fileName: "Attachment",
        loadingSpinner: loadingSpinner,
        t: 0
      };
    },
    computed: {
      getImageUrl: function () {
        if (this.dialog) {
          return Urls.HTTP_BASE + '/downloadAttachment?filename=' + this.fileName;
        } else {
          return loadingSpinner;
        }
      }
    },
    methods: {
      download: function () {
        const imageUrl = Urls.HTTP_BASE + '/downloadAttachment?filename=' + this.fileName;
        saveAs(imageUrl, this.fileName);
      }
    },
    created () {
      const vm = this;
      eventBus.$on(EventName.ATTACHMENT_POPUP, function (value) {
        vm.dialog = value.open;
        vm.fileName = value.fileName;
        vm.t = new Date().getTime();
      });
    }
  };
</script>
