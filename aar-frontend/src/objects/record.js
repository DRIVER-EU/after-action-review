export function Record(record) {
  this.id = record.id,
   this.clientId = record.clientId,
    this.topic = record.topic,
    this.createDate = null,
    this.createTime = null,
    this.recordType = record.recordType,
    this.recordJson = null,
    this.recordData = record.recordData
}
