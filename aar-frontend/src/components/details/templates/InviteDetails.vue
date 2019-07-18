<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="ID" :value="record.recordData.id"></DetailsRow>
    <DetailsRow title="Topic name" :value="record.recordData.topicName"></DetailsRow>
    <DetailsRow title="Subscribing allowed" :value="record.recordData.subscribeAllowed"></DetailsRow>
    <DetailsRow title="Publishing allowed" :value="record.recordData.publishAllowed"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'InviteDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsAttachments},
  };
</script>

<style scoped>

</style>
