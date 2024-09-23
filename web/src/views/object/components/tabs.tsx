import Details from './tabs/details/index.vue';
import RowDrag from './tabs/sensing-data/index.vue';

export const tabs = [
    {
        key: 'details',
        content: 'common.details',
        title: 'common.details',
        component: Details,
    },
    {
        key: 'rowDrag',
        content: 'object.sensingData',
        title: 'object.sensingData',
        component: RowDrag,
    },
];
