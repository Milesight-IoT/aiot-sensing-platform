import { $t } from '@/plugins/i18n';
const renderTableHeader = row => {
    console.log(row.column?.label);
    return $t(row.column?.label);
};

export { renderTableHeader };
