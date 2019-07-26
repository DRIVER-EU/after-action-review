<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Client ID" :value="record.recordData ? record.recordData.id : null"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'FallbackDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsAttachments},
  };
</script>

<style scoped>

</style>
