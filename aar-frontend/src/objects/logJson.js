export function logJson(logJson) {
  this.id = logJson.id,
    this.dateTimeSent = logJson.dateTimeSent,
    this.level = logJson.level,
    this.log = logJson.log
}
