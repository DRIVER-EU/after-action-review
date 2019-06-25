<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="ID" :value="record.recordData.id"></DetailsRow>
    <DetailsRow title="Topic name" :value="record.recordData.topicName"></DetailsRow>
    <DetailsRow title="Subscribing allowed" :value="record.recordData.subscribeAllowed"></DetailsRow>
    <DetailsRow title="Publishing allowed" :value="record.recordData.publishAllowed"></DetailsRow>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';

  export default {
    name: 'InviteDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree},
  };
</script>

<style scoped>

</style>
