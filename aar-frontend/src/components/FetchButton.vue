<template>
  <v-btn @click.prevent="performDownload()" :class="className" :disabled="disabled || loading">
    <div v-if="loading" style="display:inline;margin-right:16px;width:24px;height:24px;">
      <v-progress-circular :indeterminate="true" size="18" width="2"></v-progress-circular>
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
    props: ['url', 'method', 'className', 'icon', 'disabled', 'onSuccess', 'onError'],
    data: function () {
      return {
        loading: false,
      };
    },
    methods: {
      performDownload () {
        const methodName = this.method || "GET";
        const method = methodName.toLowerCase() === "post" ? fetchService.performPost.bind(fetchService) : fetchService.performGet.bind(fetchService);
        const me = this;
        me.loading = true;
        method(Urls.HTTP_BASE + this.url).then(response => {
          me.loading = false;
          if (me.onSuccess) {
            me.onSuccess();
          }
        }).catch(e => {
          me.loading = false;
          if (me.onError) {
            me.onError(e);
          }
        });
      }
    },
    created: function () {
    }
  };
</script>
