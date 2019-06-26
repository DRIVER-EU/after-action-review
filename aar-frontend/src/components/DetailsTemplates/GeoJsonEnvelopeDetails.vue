<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Client ID" :value="record.clientId"></DetailsRow>
    <DetailsRow title="Date/Time" :value="record.createDate + ' ' + record.createTime"></DetailsRow>
    <DetailsRow title="Level" :value="record.level"></DetailsRow>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';

  export default {
    name: 'GeoJsonEnvelopeDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree},
  };
</script>

<style scoped>

</style>
