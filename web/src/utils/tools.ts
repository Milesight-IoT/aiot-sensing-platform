/*
 * @Author: lin@milesight.com
 * @Date: 2023-02-06 20:50:39
 * @LastEditTime: 2023-07-10 20:36:09
 * @Descripttion: AIoT Sensing Platform
 */
import dayjs from 'dayjs';
import { Parser } from '@json2csv/plainjs';
import { renderTableHeader } from '@/utils/rendertool';
const dateTimeFormat = (row, col) => {
    const createTime = row[col.property];
    return dayjs(createTime).format('YYYY-MM-DD HH:mm:ss');
};
const getDateTime = (createTime, format = 'YYYY-MM-DD HH:mm:ss') => {
    return dayjs(createTime).format(format);
};
const downloadCsv = (dataLists, fileName: String, opts = {}) => {
    const parser = new Parser(opts);
    const csv = parser.parse(dataLists);
    const csvContent = 'data:text/csv;charset=utf-8,\uFEFF' + csv;
    // 非ie 浏览器
    createDownLoadClick(csvContent, fileName);
};
const downloadJson = (data, fileName) => {
    const jsonBlob = new Blob([JSON.stringify(data)], {
        type: 'data:application/json;charset=utf-8',
    });
    createDownLoadClick(jsonBlob, fileName, true);
};
const downloadBase64Img = (base64Data, fileName) => {
    const picHref = `${base64Data}`; //Image Base64 Goes here
    createDownLoadClick('', fileName, false, picHref);
};
function createDownLoadClick(content, fileName, isBlob?, href?) {
    const link = document.createElement('a');
    link.href = href || (isBlob ? window.URL.createObjectURL(content) : encodeURI(content));
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
const getTableColums = columns => {
    return columns.map(item => {
        if (item.label) {
            item.headerRenderer = renderTableHeader;
        }
        return item;
    });
};
// 深度合并对象的函数
// 深度合并对象的函数
const mergeDeep = (target, source) => {
    // 检查类型
    if (typeof target !== 'object' || typeof source !== 'object') {
        return source;
    }

    // 遍历源对象的键
    for (const key in source) {
        // 如果键存在于目标对象中
        if (key in target) {
            // 递归地合并深层对象
            target[key] = mergeDeep(target[key], source[key]);
        } else {
            // 否则，将源对象的键值对添加到目标对象中
            target[key] = source[key];
        }
    }

    return target;
};

// 深度获取对象属性的函数
const getDeepValue = (obj, path) => {
    const keys = path.split('.');
    let value = obj;
    for (const key of keys) {
        value = value[key];
        if (value === undefined) {
            return undefined;
        }
    }
    return value;
};

// 合并并去重深层对象数组(通过对象的key)
const mergedArray = (array1, array2, key) => {
    const newArr = Array.from(
        new Map(
            [...array1, ...array2].map(obj => [getDeepValue(obj, key), obj])
        ).values()
    )
    return newArr
}

const mergedArrayByKey = (array1, array2, key, subKey?) => {
    let newArr = []
    if (subKey) {
        newArr = Array.from(
            new Map(
                [...array1, ...array2].map(obj => [obj[key][subKey], obj])
            ).values()
        );
    } else {
        newArr = Array.from(
            new Map(
                [...array1, ...array2].map(obj => [obj[key], obj])
            ).values()
        );
    }
    return newArr

}
export { dateTimeFormat, getDateTime, downloadCsv, downloadJson, downloadBase64Img, getTableColums, mergedArray, mergedArrayByKey };
