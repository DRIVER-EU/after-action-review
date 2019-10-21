<template>
  <v-btn @click.prevent="performDownload()" :class="className">
    <div v-if="loading" style="display:inline;margin-right:16px;width:24px;height:24px;">
      <v-progress-circular indeterminate="true" size="18" width="2"></v-progress-circular>
    </div>
    <v-icon v-else left>{{icon}}</v-icon>
    <slot/>
  </v-btn>
</template>

<script>
  import {fetchService} from '../service/FetchService';
  import Urls from '../constants/Urls';

  export default {
    name: 'FetchButton',
    props: ['url', 'className', 'icon'],
    data: function () {
      return {
        loading: false,
      };
    },
    methods: {
      performDownload () {
        const me = this;
        me.loading = true;
        fetchService.performGet(Urls.HTTP_BASE + this.url).then(response => {
          me.loading = false;
        });
      }
    },
    created: function () {
    }
  };
</script>
