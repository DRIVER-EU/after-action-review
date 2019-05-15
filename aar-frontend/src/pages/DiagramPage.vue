<template>
  <v-app>
    <toolbar>
      <v-btn @click.prevent="download()" class="diagramButton">
        <v-icon left>save_alt</v-icon>
        Download
      </v-btn>
    </toolbar>
    <main>
      <div class="text-xs-center">
        <v-flex>
          <v-card style="padding: 30px 0px;">
            <vue-load-image class="diagramImage">
              <img slot="image" :key="imageVersion" :src="getImageUrl"/>
              <img slot="preloader" :src="loadingSpinner"/>
              <div slot="error">Missing image.</div>
            </vue-load-image>
          </v-card>
        </v-flex>
      </div>
    </main>
  </v-app>
</template>
<script>
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import Urls from '../constants/Urls';
  import {saveAs} from 'file-saver';
  import loadingSpinner from '../assets/loading-spinner.gif';

  export default {
    data () {
      return {
        loadingSpinner: loadingSpinner,
        imageVersion: 0,
      };
    },
    computed: {
      getImageUrl: function () {
        return Urls.HTTP_BASE + '/createSequenceDiagram?t=' + new Date().getTime() + '&v=' + this.imageVersion; // http://localhost:8095/AARService/createSequenceDiagram
      }
    },
    methods: {
      download: function () {
        const imageUrl = Urls.HTTP_BASE + '/createSequenceDiagram?t=' + new Date().getTime() + '&v=' + this.imageVersion;
        saveAs(imageUrl, 'sequenceDiagram.png');
      }
    },
    created () {
      const vm = this;
      eventBus.$on(EventName.RECORD_NOTIFICATION, function (value) {
        vm.imageVersion += 1;
      });
    }
  };
</script>
