<template>
  <li>
    <v-layout row>
      <v-flex xs4>
        <v-subheader style="align-items: normal;">{{title}}:</v-subheader>
      </v-flex>
      <v-flex xs8>
        <ul class="attachments">
          <li v-for="item in value">
            <a v-if="isImage(item.name)" @click="openImagePopup(item.name)">{{ item.name }}</a>
            <a v-else :href="getDownloadUrl(item.name)">{{ item.name }}</a>
          </li>
        </ul>
      </v-flex>
    </v-layout>
  </li>
</template>

<script>
  import EventName from '../../../constants/EventName';
  import Urls from '../../../constants/Urls';

  export default {
    name: 'DetailsAttachments',
    props: ['title', 'value'],
    methods: {
      isImage(fileName) {
        const lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".png") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || lowerFileName.endsWith(".gif");
      },
      openImagePopup (fileName) {
        eventBus.$emit(EventName.ATTACHMENT_POPUP, {open: true, fileName: fileName});
      },
      getDownloadUrl(fileName) {
        return Urls.HTTP_BASE + '/downloadAttachment?filename=' + fileName;
      }
    }
  };
</script>

<style scoped>

</style>
