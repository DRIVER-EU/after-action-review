<template>
  <ul style="list-style-type: none;">
    <DetailsRow title="Record ID" :value="recordID"></DetailsRow>
    <DetailsRow title="Headline" :value="record.headline"></DetailsRow>
    <DetailsRow title="Date/Time" :value="formatDateTime(record.recordData.timeSendUTC)"></DetailsRow>
    <DetailsRow title="Type" :value="record.recordData.observationTypeName"></DetailsRow>
    <span v-for="question in record.recordData.questions">
      <DetailsRow :title="question.name" :value="question.answer"></DetailsRow>
    </span>
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
    name: 'ObserverToolAnswerDetails',
    props: ['recordID', 'record'],
    components: {DetailsRow, DetailsJsonTree, DetailsAttachments},
    methods: {
      formatDateTime: function(date) {
        return formatDate(date, 'YYYY-MM-DD HH:mm:ss.SSS');
      }
    }
  };
</script>

<style scoped>

</style>
