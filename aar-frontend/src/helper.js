import moment from 'moment'

export function parseDate(date, format) {
 return moment.utc(new Date(date)).format(format)
}
