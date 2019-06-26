import moment from 'moment'

export function formatDate(date, format) {
 return moment.utc(new Date(date)).format(format)
}
