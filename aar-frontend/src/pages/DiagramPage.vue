<template>
  <v-app>
    <toolbar>
      <v-btn @click.prevent="download()" :disabled="loading" class="diagramButton">
        <v-icon left>save_alt</v-icon>
        Download
      </v-btn>
    </toolbar>
    <main>
      <div class="text-xs-center">
        <v-flex>
          <v-card style="padding: 30px 0px;">
            <div class="diagramImage">
              <img v-show="loading" :src="loadingSpinner"/>
              <img v-show="!loading" :src="imageUrl" style="max-width: 100%;"/>
            </div>
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
  import {fetchService} from '../service/FetchService';

  export default {
    name: 'DiagramPage',
    props: {
      'relativeImageUrl': {
        type: String
      },
      'downloadFileName': {
        type: String
      },
      'reloadOnRecordNotification': {
        type: Boolean
      }
    },
    data () {
      return {
        loading: false,
        loadingSpinner: loadingSpinner,
        imageVersion: 0,
        imageBlob: null,
        imageUrl: null,
      };
    },
    methods: {
      download: function () {
        if (this.imageBlob) {
          saveAs(this.imageBlob, this.downloadFileName);
        }
      },
      reloadImage: function() {
        const vm = this;
        const imageUrl = Urls.HTTP_BASE + this.relativeImageUrl + '?t=' + new Date().getTime() + '&v=' + this.imageVersion;
        vm.imageVersion += 1;
        vm.loading = true;
        fetchService.performGet(imageUrl).then(response => {
          const svg = response.data;
          vm.imageBlob = new Blob([svg], {type: 'image/svg+xml'});
          vm.imageUrl = URL.createObjectURL(vm.imageBlob);
          vm.loading = false;
        }).catch(ex => console.log(ex));
      }
    },
    created () {
      const vm = this;
      eventBus.$on(EventName.RECORD_NOTIFICATION, function (value) {
        if (vm.reloadOnRecordNotification) {
          vm.reloadImage();
        }
      });
      this.reloadImage();
    }
  };
</script>
