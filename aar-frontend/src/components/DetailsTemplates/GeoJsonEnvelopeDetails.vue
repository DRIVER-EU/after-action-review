<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsMap title="Map" :geojson="record.recordData.geojson"></DetailsMap>
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
  import DetailsMap from './DetailsMap';

  export default {
    name: 'GeoJsonEnvelopeDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsMap},
  };
</script>

<style scoped>

</style>
