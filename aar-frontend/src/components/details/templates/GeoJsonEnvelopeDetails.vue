<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsMap title="Map" :geojson="record.recordData.geojson"></DetailsMap>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Client ID" :value="record.clientId"></DetailsRow>
    <DetailsRow title="Date/Time" :value="record.createDate + ' ' + record.createTime"></DetailsRow>
    <DetailsRow title="Level" :value="record.level"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import DetailsMap from '../items/DetailsMap';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'GeoJsonEnvelopeDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsMap, DetailsAttachments},
  };
</script>

<style scoped>

</style>
