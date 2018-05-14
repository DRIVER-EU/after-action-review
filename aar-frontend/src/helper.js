export function createFilterOptions(record,filterOptions) {
  var keys = Object.keys(record)
  keys.forEach(function(key){
    if(filterOptions[key] && filterOptions[key].indexOf(record[key]) === -1 )  filterOptions[key].push(record[key])
  })
  console.log(filterOptions)
}

import moment from 'moment'
export function parseDate(date) {
  let returnDate = moment.utc(new Date(date)).format('YYYY-MM-DD HH:mm:ss.SSS')
  console.log(returnDate)
  return returnDate
}
