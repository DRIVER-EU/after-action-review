export function createFilterOptions(record,filterOptions) {
  var keys = Object.keys(record)
  keys.forEach(function(key){
    if(filterOptions[key] && filterOptions[key].indexOf(record[key]) === -1 )  filterOptions[key].push(record[key])
  })
}

import moment from 'moment'
export function parseDate(date, format) {
 return moment.utc(new Date(date)).format(format)
}
