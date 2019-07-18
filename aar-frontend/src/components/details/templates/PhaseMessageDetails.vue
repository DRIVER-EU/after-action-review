<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Phase" :value="record.recordData.phase"></DetailsRow>
    <DetailsRow title="Is Starting" :value="getBooleanDisplayValue(record.recordData.isStarting)"></DetailsRow>
    <DetailsAttachments title="Attachments" :value="record.attachments"></DetailsAttachments>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from '../items/DetailsRow';
  import DetailsJsonTree from '../items/DetailsJsonTree';
  import {formatDate} from '../../../helper';
  import DetailsAttachments from '../items/DetailsAttachments';

  export default {
    name: 'PhaseMessageDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsAttachments},
    methods: {
      getBooleanDisplayValue: function(value) {
        return value ? "Yes" : "No";
      }
    }
  };
</script>

<style scoped>

</style>
