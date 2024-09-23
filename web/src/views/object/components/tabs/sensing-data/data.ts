import dayjs from 'dayjs';
import { clone } from '@pureadmin/utils';

const createdTime = dayjs(new Date()).format('YYYY-MM-DD');

const tableData = [
    {
        createdTime,
        value: 'Tom',
        abilityType: 1,
    },
    {
        createdTime,
        value: 'Jack',
        abilityType: 1,
    },
    {
        createdTime,
        value: 'Dick',
        abilityType: 1,
    },
    {
        createdTime,
        value: '-',
        abilityType: 2,
    },
    {
        createdTime,
        value: 'Sam',
        abilityType: 1,
    },
    {
        createdTime,
        value: 'Lucy',
        abilityType: 1,
    },
    {
        createdTime,
        value: 'Mary',
        abilityType: 1,
    },
    {
        createdTime,
        value: '-',
        abilityType: 2,
    },
];

const tableDataMore = clone(tableData, true).map(item =>
    Object.assign(item, {
        state: 'California',
        city: 'Los Angeles',
        'post-code': 'CA 90036',
    }),
);

const tableDataImage = clone(tableData, true).map((item, index) =>
    Object.assign(item, {
        image: `https://xiaoxian521.github.io/pure-admin-table/imgs/${index + 1}.jpg`,
    }),
);

const tableDataSortable = clone(tableData, true).map((item, index) => {
    delete item['createdTime'];
    Object.assign(item, {
        createdTime: `${dayjs(new Date()).format('YYYY-MM')}-${index + 1}`,
    });
});

const tableDataDrag = clone(tableData, true).map((item, index) => {
    delete item['abilityType'];
    delete item['createdTime'];
    return Object.assign(
        {
            id: index + 1,
            createdTime: `${dayjs(new Date()).format('YYYY-MM')}-${index + 1}`,
        },
        item,
    );
});

const tableDataEdit = clone(tableData, true).map((item, index) => {
    delete item['createdTime'];
    return Object.assign(
        {
            id: index + 1,
            createdTime: `${dayjs(new Date()).format('YYYY-MM')}-${index + 1}`,
        },
        item,
    );
});

export { tableData, tableDataDrag, tableDataMore, tableDataEdit, tableDataImage, tableDataSortable };
