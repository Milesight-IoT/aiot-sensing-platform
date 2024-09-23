import Details from './tabs/details/index.vue';
import { $t } from '@/plugins/i18n';

export const tabs = [
    {
        key: 'details',
        content: $t('common.details'),
        title: $t('common.details'),
        component: Details,
    },
];
