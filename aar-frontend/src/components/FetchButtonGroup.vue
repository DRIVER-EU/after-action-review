<template>
  <v-menu offset-y content-class="dropdown-menu" transition="slide-y-transition" :disabled="loading">
    <v-btn slot="activator">
      <div v-if="loading" style="display:inline;margin-right:16px;width:24px;height:24px;">
        <v-progress-circular :indeterminate="true" size="18" width="2"></v-progress-circular>
      </div>
      <v-icon v-else left>{{icon}}</v-icon>
      <slot/>
    </v-btn>
    <v-card>
      <v-list>
        <v-list-tile v-for="(item, index) in buttons" :key="index" @click="performDownload(item.url)">
          <v-icon left>{{item.icon}}</v-icon> <!-- arrow_downward -->
          <v-list-tile-title v-text="item.title"/>
        </v-list-tile>
      </v-list>
    </v-card>
  </v-menu>
</template>

<script>
  import {fetchService} from '../service/FetchService';
  import Urls from '../constants/Urls';

  export default {
    name: 'FetchButtonGroup',
    props: ['icon', 'buttons'],
    data: function () {
      return {
        loading: false,
      };
    },
    methods: {
      performDownload (url) {
        const me = this;
        me.loading = true;
        fetchService.performGet(Urls.HTTP_BASE + url).then(response => {
          me.loading = false;
        });
      }
    },
    created: function () {
    }
  };
</script>
