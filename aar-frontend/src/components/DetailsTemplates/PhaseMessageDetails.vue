<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Phase" :value="record.recordData.phase"></DetailsRow>
    <DetailsRow title="Is Starting" :value="getBooleanDisplayValue(record.recordData.isStarting)"></DetailsRow>
    <DetailsJsonTree title="Message" :value="record.recordJson"></DetailsJsonTree>
  </ul>
</template>

<script>
  import DetailsRow from './DetailsRow';
  import DetailsJsonTree from './DetailsJsonTree';
  import {formatDate} from '../../helper';

  export default {
    name: 'PhaseMessageDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree},
    methods: {
      getBooleanDisplayValue: function(value) {
        return value ? "Yes" : "No";
      }
    }
  };
</script>

<style scoped>

</style>
