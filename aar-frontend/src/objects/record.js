export function Record (record) {
    this.id = record.id;
    this.clientId = record.clientId;
    this.topic = record.topic;
    this.headline = record.headline;
    this.msgType = record.msgType;
    this.createDate = null;
    this.createTime = null;
    this.recordType = record.recordType;
    this.recordJson = record.recordJson;
    this.recordData = record.recordData;
    this.runType = record.runType;
    this.attachments = record.attachments;
}
