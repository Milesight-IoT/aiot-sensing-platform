
import { transformI18n } from '@/plugins/i18n'
const checkNumberic = (rule, val, cb) => {
  if (/[^0-9]/.test(val)) cb(new Error(transformI18n('numerField')))
  else cb()
}
const checkIntValidRange = (rule, val, cb, minVal, maxVal, outTip) => {
  let errorTip = `${outTip}: ${minVal}~${maxVal}!`
  if (typeof val == 'string' && val.trim() === '') {
    cb(new Error(errorTip))
  } else if (/[^0-9]/.test(val)) {
    cb(new Error(errorTip))
  } else {
    if (parseInt(val) < minVal || parseInt(val) > maxVal) {
      cb(new Error(errorTip))
    } else {
      cb()
      return 0
    }
  }
  return -1
}
const checkPort = (rule, val, cb) => {
  checkIntValidRange(rule, val, cb, 1, 65535, transformI18n('ruleTips.validRange'))
}
function forbitChars(str, spec) {
  for (let i = 0; i < str.length; i++) {
    let mChar = str.charAt(i)
    if (spec.indexOf(mChar) >= 0) return false
  }

  return true
}
const checkAlphaNumeric = (rule, val, cb) => {
  if (/[^a-zA-Z0-9_-]/.test(val)) cb(new Error(transformI18n('numberLetter')))
  else cb()
}
//判断字节长度是否超过，1个中文当做3个字符
const checkLimitLen = (rule, val, cb, len) => {
  var r = /[^\x00-\xff]/g
  if (val.replace(r, 'mmm').length > len) {
    cb(new Error(transformI18n(`lenLimit${len}`)))
  } else {
    cb()
  }
}
const checkRequired = (rule, val, cb) => {
  val = val && val.trim()
  if (!val) cb(new Error(transformI18n('requiredField')))
  else cb()
}

export { checkNumberic, checkIntValidRange, checkPort, forbitChars, checkAlphaNumeric, checkLimitLen, checkRequired }
