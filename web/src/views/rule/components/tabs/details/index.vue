<!--
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-09-07 20:26:09
 * @Descripttion: 规则右侧详情
-->

<script setup lang="ts">
import { ref, watch, onMounted, nextTick, unref } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance, CascaderProps, Cascader } from 'element-plus';
import { emitter } from '@/utils/mitt';
import { addRule, getRuleByName } from '@/api/rules';
import { getUserDeviceList, getDeviceListById } from '@/api/device';
import { getSensingObjectList, getSensingObjectById, getSensingObjectSelectList } from '@/api/sensing-object';
import { getRecipientList, getRecipientListById } from '@/api/recipient';
import { getRoiAbility, getSelRoiAbility } from '@/api/device-ability';
import { mergedArrayByKey } from '@/utils/tools';
import { triggerOpts, actionOpts } from '../../data';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {
                jsonData: {},
            };
        },
    },
});
const ruleFormRef = ref<FormInstance>();
const formData = ref(props.data);
watch(
    () => props.data,
    val => {
        initData(val);
    },
);
const rules = {
    name: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (textSearch) {
                    if (textSearch == props.data.name) callback();
                    // 名字不存在时返回值空空如也
                    const { name } = await getRuleByName(textSearch);
                    if (!name) {
                        callback();
                    } else {
                        callback($t('rules.existRule'));
                    }
                } else {
                    callback($t('common.requiredField'));
                }
            },
        },
    ],
    actions: {
        required: true,
        trigger: 'blur',
        message: () => $t('common.requiredField'),
    },
    'jsonData.recipients': {
        required: true,
        validator: async (rule, value, callback) => {
            const showRecip =
                formData.value.actions == 'Send to recipients' ||
                formData.value.actions?.includes('Send to recipients');
            if (!showRecip) {
                callback();
            }
            if (value.length == 0) {
                callback($t('common.requiredField'));
            }
            if (value.length > 4) {
                callback($t('rules.pleaseSelRecipients'));
            } else {
                callback();
            }
        },
        trigger: 'change',
    },
    'jsonData.threshold': {
        required: true,
        trigger: 'blur',
        message: () => $t('common.requiredField'),
    },
};

function changeActions(val) {
    console.log('changeActions', val);
    if (!val.includes('Send to recipients')) {
        formData.value.jsonData.recipients = [];
    }
}

/** Join改造，字符串则直接返回 */
String.prototype.msplit = function (splitter) {
    if (this == '') {
        return this;
    } else {
        return this.split(splitter);
    }
};

async function initData(rawData) {
    console.log('right dialog form initData', rawData);
    let jsonData = {};
    const trigger = rawData.trigger;
    let actions = null;
    //loadingOpts.value = true;
    if (trigger == 'Once data received') {
        if (rawData.jsonData.sensingObjectIds) {
            await getSelObjectList(rawData.jsonData.sensingObjectIds);
            jsonData.sensingObjectIds = objectList.value.map(item => item.id);
        } else {
            getObjectList();
        }
        actions = rawData.actions;
    } else if (trigger == 'Once result recognized') {
        if (rawData.jsonData?.abilityIds) {
            const abilityIds = rawData.jsonData?.abilityIds,
                deviceIds = rawData.jsonData?.deviceIds;
            await getSelRoiList(deviceIds);
            const deviceArr = deviceIds?.split(',');
            jsonData.channels = abilityIds?.split(',').map((item, index) => {
                if (item) return [deviceArr[index], item];
            });
        } else {
            //initRoiChannels();
            jsonData.channels = [];
        }
        actions = rawData.actions;
    } else {
        if (rawData.jsonData.deviceIds) {
            await getSelDeviceList(rawData.jsonData.deviceIds);
            jsonData.deviceIds = deviceList.value.map(item => item.id?.id);
        } else {
            getDeviceList();
        }
        actions = rawData.actions.msplit(',');
    }
    if (rawData.jsonData && (actions == 'Send to recipients' || actions?.includes('Send to recipients'))) {
        if (rawData.jsonData.recipients) {
            getSelRecipientList(rawData.jsonData.recipients);
            jsonData.recipients = rawData.jsonData.recipients.msplit(',');
        } else {
            getRecipientsList();
        }
    }
    jsonData = Object.assign({}, rawData.jsonData, { ...jsonData });
    formData.value = {
        ...rawData,
        trigger,
        actions,
        jsonData,
    };
    resetForm(ruleFormRef.value);
}
const getSelDeviceList = async deviceIds => {
    const params = {
        deviceIds,
    };
    const tmpData = await getDeviceListById(params);
    deviceList.value = tmpData;
    filterDeviceListTmp = [];
    loadingOpts.value = false;
};
const getSelObjectList = async sensingObjectsIds => {
    const params = {
        sensingObjectsIds,
        needChannels: true,
        abilityType: 2,
    };
    const tmpData = await getSensingObjectById(params);
    objectList.value = tmpData;
    filterObjectListTmp = [];
    loadingOpts.value = false;
};
const getSelRecipientList = async recipientsIds => {
    const params = {
        recipientsIds,
    };
    const tmpData = await getRecipientListById(params);
    filterRecipientListTmp = [];
    recipientList.value = tmpData;
};

const getSelRoiList = async deviceIds => {
    const params = {
        deviceIds,
        abilityType: 2,
        isRoi: true,
    };
    const tmpData = await getSelRoiAbility(params);
    const nodes = tmpData?.map(item => {
        const children = item.childSelectData.map(item => {
            return { label: item.ability, value: item.abilityId, leaf: true };
        });
        return { children, label: item.deviceName, value: item.deviceId };
    });
    casOptions.value = nodes || [];
    console.log(nodes);
};

//获取下拉动态数据
let queryStr = '';
const deviceList = ref([]);
let filterDeviceListTmp = [];
const loadingOpts = ref(true);
const getDevicesSelList = dataList => {
    const deviceIds = formData.value.jsonData?.deviceIds || [];
    const filterData = deviceList.value.filter(item => {
        return deviceIds.includes(item.id.id);
    });
    const newData = mergedArrayByKey(filterData, dataList, 'id', 'id');
    return newData;
};
const getDeviceList = async (
    queryParams = {
        pageSize: 100,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'ASC',
    },
) => {
    if (filterDeviceListTmp.length) return;
    const { data } = await getUserDeviceList(queryParams);
    let tmpData = getDevicesSelList(data);
    deviceList.value = tmpData;
    filterDeviceListTmp = tmpData;
    loadingOpts.value = false;
};
const changeDevice = async () => {
    await getDeviceList();
    const tempData = [...deviceList.value];
    filterDeviceListTmp = getDevicesSelList(filterDeviceListTmp);
    deviceList.value = filterDeviceListTmp;
    if (queryStr) {
        nextTick(() => {
            deviceList.value = tempData;
            //getFilterObject(queryStr);
        });
    }
};
const getFilterDevice = async (query: string) => {
    if (query) {
        const queryParams = {
            pageSize: 100,
            page: 0,
            textSearch: query,
            sortProperty: 'name',
            sortOrder: 'ASC',
        };
        const { data } = await getUserDeviceList(queryParams);
        deviceList.value = data;
        queryStr = query;
    } else {
        filterDeviceListTmp = getDevicesSelList(filterDeviceListTmp);
        deviceList.value = filterDeviceListTmp;
        queryStr = '';
    }
};

const objectList = ref([]);
let filterObjectListTmp = [];
const getObjectsSelList = dataList => {
    const sensingObjectIds = formData.value.jsonData?.sensingObjectIds || [];
    const filterData = objectList.value.filter(item => {
        return sensingObjectIds.includes(item.id);
    });
    const newData = mergedArrayByKey(filterData, dataList, 'id');
    return newData;
};
const getObjectList = async (
    queryParams = {
        pageSize: 100,
        page: 0,
        needChannels: true,
    },
) => {
    if (filterObjectListTmp.length) return;
    const { data } = await getSensingObjectSelectList(queryParams);
    let tmpData = getObjectsSelList(data);
    objectList.value = tmpData;
    filterObjectListTmp = tmpData;
    loadingOpts.value = false;
};
const changeObject = async () => {
    await getObjectList();
    const tempData = [...objectList.value];
    filterObjectListTmp = getObjectsSelList(filterObjectListTmp);
    objectList.value = filterObjectListTmp;
    if (queryStr) {
        nextTick(() => {
            objectList.value = tempData;
            //getFilterObject(queryStr);
        });
    }
};
const getFilterObject = async (query: string) => {
    if (query) {
        const queryParams = {
            pageSize: 100,
            page: 0,
            textSearch: query,
            needChannels: true,
            sortOrder: 'ASC',
        };
        const { data } = await getSensingObjectSelectList(queryParams);
        objectList.value = data;
        queryStr = query;
    } else {
        filterObjectListTmp = getObjectsSelList(filterObjectListTmp);
        objectList.value = filterObjectListTmp;
        queryStr = '';
    }
};

const recipientList = ref([]);
let filterRecipientListTmp = [];
const getRecipientsSelList = dataList => {
    const recipients = formData.value.jsonData?.recipients || [];
    const filterData = recipientList.value.filter(item => {
        return recipients.includes(item.recipientsId.id);
    });
    const newData = mergedArrayByKey(filterData, dataList, 'recipientsId', 'id');
    return newData;
};
const getRecipientsList = async (
    queryParams = {
        pageSize: 100,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'ASC',
    },
) => {
    if (filterRecipientListTmp.length) return;
    const { data } = await getRecipientList(queryParams);
    let tmpData = getRecipientsSelList(data);
    filterRecipientListTmp = tmpData;
    recipientList.value = tmpData;
    //loadingOpts.value = false;
};
const changeRecipient = async () => {
    await getRecipientsList();
    const tempData = [...recipientList.value];
    filterRecipientListTmp = getRecipientsSelList(filterRecipientListTmp);
    recipientList.value = filterRecipientListTmp;
    if (queryStr) {
        nextTick(() => {
            recipientList.value = tempData;
            //getFilterObject(queryStr);
        });
    }
};
const getFilterRecipient = async (query: string) => {
    if (query) {
        const queryParams = {
            pageSize: 100,
            page: 0,
            textSearch: query,
            sortProperty: 'name',
            sortOrder: 'ASC',
        };
        const { data } = await getRecipientList(queryParams);
        recipientList.value = getRecipientsSelList(data);
        queryStr = query;
    } else {
        filterRecipientListTmp = getRecipientsSelList(filterRecipientListTmp);
        recipientList.value = filterRecipientListTmp;
        queryStr = '';
    }
};

//获取ROI Channels
const showPopper = ref(false);
const casOptions = ref([]);
const casProps: CascaderProps = {
    multiple: true,
};
const casRef = ref<Cascader>();
const casOpts = ref([]);
const casPanelProps: CascaderProps = {
    multiple: true,
};
const filterChannels = ref([]);
const hasMatchData = ref(false);

const getAbility = async (textSearch?) => {
    console.log('getInitRoi');
    const queryParams = {
        textSearch,
        abilityType: 2,
        isRoi: true,
        pageSize: 100,
        page: 0,
    };
    const { data } = await getRoiAbility(queryParams);
    const nodes = data.map(item => {
        const children = item.childSelectData.map(item => {
            return { label: item.ability, value: item.abilityId, leaf: true };
        });
        return { children, label: item.deviceName, value: item.deviceId };
    });
    return nodes;
};
async function initRoiChannels(isShow) {
    if (isShow) {
        const tmpData = await getAbility();
        casOptions.value = mergedArrayByKey(casOptions.value, tmpData, 'value');
    }
}

const filterMethod = async textSearch => {
    casOpts.value = await getAbility(textSearch);
    hasMatchData.value = casOpts.value.length > 0;
    filterChannels.value = unref(formData.value.jsonData.channels);
    valueBeforeChannels = unref(formData.value.jsonData.channels);
    return true;
};
const focusPanel = event => {
    event.preventDefault();
};
let valueBeforeChannels = [];
const filteChange = value => {
    casOptions.value = mergedArrayByKey(casOptions.value, casOpts.value, 'value');
    nextTick(() => {
        formData.value.jsonData.channels = [...valueBeforeChannels, ...value];
    });
};
const casChange = value => {
    nextTick(() => {
        filterChannels.value = unref(formData.value.jsonData.channels);
    });
};
const handleFilterMethod = (node, keyword) => {
    return false;
};
//获取下拉动态数据--结束
/** Join改造，字符串则直接返回 */
String.prototype.mjoin = function () {
    return this;
};
Array.prototype.mjoin = function (joiner) {
    return this.join(joiner);
};
const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};
const emit = defineEmits(['updateForm', 'updateTitle']);
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    const res = await formEl.validate();
    if (res) {
        try {
            let jsonData;
            if (formData.value.trigger == 'Once data received') {
                jsonData = {
                    sensingObjectIds: formData.value.jsonData.sensingObjectIds?.mjoin(), //设备ID 多个逗号隔开
                };
            } else if (formData.value.trigger == 'Low battery') {
                jsonData = {
                    threshold: formData.value.jsonData.threshold, // 电量
                    deviceIds: formData.value.jsonData.deviceIds?.mjoin(),
                };
            } else if (formData.value.trigger == 'Devices become inactive') {
                jsonData = {
                    deviceIds: formData.value.jsonData.deviceIds?.mjoin(), //设备ID 多个逗号隔开
                };
            } else if (formData.value.trigger == 'Once result recognized') {
                //console.log(formData.value.channels);
                const abilityIds = [],
                    deviceIds = [];
                formData.value.jsonData.channels?.map(item => {
                    deviceIds.push(item[0]);
                    abilityIds.push(item[1]);
                });
                jsonData = {
                    abilityIds: abilityIds.join(',') || '', //ROI能力ID 多个逗号隔开
                    deviceIds: deviceIds.join(',') || '', //ROI能力所属设备ID 多个逗号隔开
                    recipients: formData.value.jsonData.recipients.join(), // 接收方,多个逗号隔开
                };
                //console.log(jsonData);
            }
            if (
                formData.value.actions == 'Send to recipients' ||
                formData.value.actions?.includes('Send to recipients')
            ) {
                jsonData.recipients = formData.value.jsonData.recipients.mjoin(); // 接收方,多个逗号隔开
            }
            const params = {
                ...formData.value,
                trigger: formData.value.trigger,
                actions: formData.value.actions.mjoin(),
                jsonData,
            };
            await addRule(params);
            emitter.emit('panelTitle', params.name);
            emit('updateForm', params);
            message($t('status.success'));
        } catch (e) {
            console.error(e);
        }
    }
};
</script>

<template>
    <el-form ref="ruleFormRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('common.name')" prop="name">
            <el-input v-model="formData.name" :placeholder="$t('rules.pleaseEnterRuleName')" maxlength="32" clearable />
        </el-form-item>
        <el-form-item :label="$t('rules.trigger')" prop="trigger">
            <el-select v-model="formData.trigger" disabled>
                <el-option v-for="item in triggerOpts" :key="item.value" :label="$t(item.label)" :value="item.text" />
            </el-select>
        </el-form-item>
        <el-form-item
            :label="$t('rules.srcSensingObjects')"
            prop="sensingObjectIds"
            v-show="formData.trigger == 'Once data received'"
        >
            <el-select
                v-model="formData.jsonData.sensingObjectIds"
                multiple
                clearable
                filterable
                :placeholder="$t('rules.pleaseSelChannels')"
                @click="getObjectList()"
                remote
                :remote-method="getFilterObject"
                @change="changeObject()"
            >
                <el-option v-for="item in objectList" :key="item.value" :label="item.name" :value="item.id" />
            </el-select>
        </el-form-item>
        <!-- Show Threshold when seleting Low battery-->
        <el-form-item prop="jsonData.threshold" v-if="formData.trigger == 'Low battery'" :label="$t('rules.threshold')">
            <el-input-number
                v-model="formData.jsonData.threshold"
                :min="0"
                :max="99"
                :maxlenth="2"
                maxlength="2"
                controls-position="right"
                class="!w-full"
                v-input-limit:number
            />
        </el-form-item>
        <!-- Show device when selecting Low battery or Devices become inactive -->
        <el-form-item
            :label="$t('rules.srcDevice')"
            v-show="formData.trigger == 'Low battery' || formData.trigger == 'Devices become inactive'"
        >
            <!-- v-loading="loadingOpts" 去除下拉加载-->
            <el-select
                v-model="formData.jsonData.deviceIds"
                multiple
                clearable
                filterable
                :placeholder="$t('rules.anyDevices')"
                @click="getDeviceList()"
                remote
                :remote-method="getFilterDevice"
                @change="changeDevice()"
            >
                <el-option v-for="item in deviceList" :key="item.value" :label="item.name" :value="item.id.id" />
            </el-select>
        </el-form-item>
        <el-form-item
            :label="$t('rules.roiChannels')"
            prop="roiChannel"
            v-if="formData.trigger == 'Once result recognized'"
        >
            <el-cascader
                ref="casRef"
                v-model="formData.jsonData.channels"
                :options="casOptions"
                :props="casProps"
                popper-class="cas-popper"
                clearable
                filterable
                :filter-method="handleFilterMethod"
                :before-filter="filterMethod"
                @change="casChange"
                @visible-change="initRoiChannels"
                :placeholder="$t('rules.pleaseSelectChannels')"
            >
                <template #empty>
                    <el-cascader-panel
                        :options="casOpts"
                        :props="casPanelProps"
                        v-model="filterChannels"
                        @change="filteChange"
                        @focus="focusPanel"
                        v-show="hasMatchData"
                    />
                    <div v-show="!hasMatchData" class="no-data">
                        {{ $t('noMatchData') }}
                    </div>
                </template>
            </el-cascader>
        </el-form-item>
        <!-- 三种trigger模式下都有action，当选择Once data received时action固定选择send to禁用 -->
        <el-form-item
            :label="$t('rules.actions')"
            v-if="formData.trigger == 'Once data received' || formData.trigger == 'Once result recognized'"
        >
            <el-select v-model="formData.actions" disabled>
                <el-option v-for="item in actionOpts" :key="item.value" :label="$t(item.label)" :value="item.text" />
            </el-select>
        </el-form-item>
        <el-form-item :label="$t('rules.actions')" v-else prop="actions">
            <el-select
                v-model="formData.actions"
                multiple
                @change="changeActions"
                :placeholder="$t('rules.pleaseSelAction')"
            >
                <el-option v-for="item in actionOpts" :key="item.value" :label="$t(item.label)" :value="item.text" />
            </el-select>
        </el-form-item>
        <!-- 当执行动作有Send to recipients时，下方就会出现接收方配置 -->
        <el-form-item
            v-if="
                formData.jsonData &&
                (formData.actions == 'Send to recipients' || formData.actions?.includes('Send to recipients'))
            "
            prop="jsonData.recipients"
            :label="$t('menus.recipients')"
        >
            <el-select
                v-model="formData.jsonData.recipients"
                multiple
                filterable
                :placeholder="$t('rules.pleaseSelRecipients')"
                @click="getRecipientsList()"
                remote
                :remote-method="getFilterRecipient"
                @change="changeRecipient"
            >
                <el-option
                    v-for="item in recipientList"
                    :key="item.value"
                    :label="item.name"
                    :value="item.recipientsId.id"
                />
            </el-select>
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="submitForm(ruleFormRef)" class="float-right exter-btn">
        {{ $t('buttons.save') }}
    </el-button>
</template>
<style scoped lang="scss">
:deep(.el-input-number .el-input__inner) {
    text-align: left;
}
.infinite-list {
    max-height: 200px;
    overflow-y: scroll;

    &::-webkit-scrollbar {
        width: 6px;
    }

    &::-webkit-scrollbar-thumb {
        background: #ddd;
        border-radius: 20px;
    }
}
</style>
