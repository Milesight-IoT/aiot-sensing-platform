const triggerOpts = [
  {
    value: 0,
    text: 'Once data received',
    label: 'rules.onceReceive',
  },
  {
    value: 1,
    text: 'Low battery',
    label: 'rules.lowBattery',
  },
  {
    value: 2,
    text: 'Devices become inactive',
    label: 'rules.deviceInactive',
  },
  {
    value: 3,
    text: 'Once result recognized',
    label: 'rules.onceResultRecognized',
  },
];
const actionOpts = [
  {
    value: 0,
    text: 'Send to recipients',
    label: 'rules.send2Recipients',
  },
  {
    value: 1,
    text: 'Show on widget',
    label: 'rules.showOnWidget',
  },
];
export {
  triggerOpts, actionOpts
}