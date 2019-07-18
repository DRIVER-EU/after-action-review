<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="URL" :value="record.recordData.url"></DetailsRow>
    <DetailsRow title="Data Type" :value="record.recordData.dataType"></DetailsRow>
    <DetailsRow title="Title" :value="record.recordData.title"></DetailsRow>
    <DetailsRow title="Description" :value="record.recordData.description"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'LargeDataUpdateDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsAttachments},
  };
</script>

<style scoped>

</style>
