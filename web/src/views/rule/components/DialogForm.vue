<!--
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-09-06 18:00:45
 * @Descripttion: 添加规则表单
-->
<script setup lang="ts">
import { nextTick, onMounted, ref, watch, unref } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance, CascaderProps } from 'element-plus';
import { addRule, getRuleByName } from '@/api/rules';
import { getUserDeviceList } from '@/api/device';
import { getSensingObjectList, getSensingObjectSelectList } from '@/api/sensing-object';
import { getRoiAbility, getSelRoiAbility } from '@/api/device-ability';
import { getRecipientList } from '@/api/recipient';
import { triggerOpts, actionOpts } from './data';
import { mergedArrayByKey } from '@/utils/tools';
const props = defineProps({
    visible: {
        type: Boolean,
        default: false,
    },
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});

const emit = defineEmits(['update:visible', 'updateTable']);
const ruleFormRef = ref<FormInstance>();
const formVisible = ref(false);
const formData = ref(props.data);
const rules = {
    name: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (textSearch) {
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
    'jsonData.recipients': [
        {
            required: true,
            trigger: 'change',
            validator: async (rule, value, callback) => {
                const showRecip =
                    formData.value.actions == 'Send to recipients' ||
                    formData.value.actions?.includes('Send to recipients');
                if (!showRecip) {
                    callback();
                }
                if (!value || value.length == 0) {
                    callback($t('common.requireFailed'));
                }
                const isInvalid = value.length > 4;
                if (isInvalid) {
                    callback($t('rules.pleaseSelRecipients'));
                } else {
                    callback();
                }
            },
        },
    ],
    'jsonData.threshold': {
        required: true,
        trigger: 'blur',
        message: () => $t('common.requiredField'),
    },
};

/** 切换选择触发器时， */
function changeTrigger(val) {
    ruleFormRef.value.resetFields(['jsonData.threshold', 'actions', 'jsonData.recipients']);
    formData.value.jsonData.sensingObjectIds = [];
    formData.value.jsonData.deviceIds = [];
    if (val == 'Once data received' || val == 'Once result recognized') {
        formData.value.actions = 'Send to recipients';
    } else {
        formData.value.actions = [];
    }
}

function changeActions(val) {
    if (!val.includes('Send to recipients')) {
        formData.value.jsonData.recipients = [];
    }
}

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
const changeDevice = () => {
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
const changeObject = () => {
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
const changeRecipient = () => {
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
const casOpts = ref([]);
const casPanelProps: CascaderProps = {
    multiple: true,
};
const filterChannels = ref([]);
const hasMatchData = ref(false);

const getAbility = async (textSearch?) => {
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
async function initRoiChannels() {
    casOptions.value = await getAbility();
}

const filterMethod = async textSearch => {
    casOpts.value = await getAbility(textSearch);
    hasMatchData.value = casOpts.value.length > 0;
    filterChannels.value = unref(formData.value.channels);
    valueBeforeChannels = unref(formData.value.channels);
    return true;
};
const focusPanel = event => {
    event.preventDefault();
};
let valueBeforeChannels = [];
const filteChange = value => {
    casOptions.value = mergedArrayByKey(casOptions.value, casOpts.value, 'value');
    nextTick(() => {
        formData.value.channels = [...valueBeforeChannels, ...value];
    });
};
const casChange = value => {
    nextTick(() => {
        filterChannels.value = unref(formData.value.channels);
    });
};
const handleFilterMethod = (node, keyword) => {
    return false;
};
/** Join改造，字符串则直接返回 */
String.prototype.mjoin = function (joiner) {
    if (Array.isArray(this)) {
        return this.join(joiner);
    } else {
        return this;
    }
};

const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async valid => {
        if (valid) {
            formVisible.value = false;
            let jsonData;
            if (formData.value.trigger == 'Once data received') {
                jsonData = {
                    sensingObjectIds: formData.value.jsonData.sensingObjectIds?.mjoin(), //感知对象ID 多个逗号隔开
                    recipients: formData.value.jsonData.recipients.join(), // 接收方,多个逗号隔开
                };
            } else if (formData.value.trigger == 'Low battery') {
                jsonData = {
                    threshold: formData.value.jsonData.threshold, // 电量
                    deviceIds: formData.value.jsonData.deviceIds?.mjoin(),
                    recipients: formData.value.jsonData.recipients.join(),
                };
            } else if (formData.value.trigger == 'Devices become inactive') {
                jsonData = {
                    deviceIds: formData.value.jsonData.deviceIds?.mjoin() || '', //设备ID 多个逗号隔开
                    recipients: formData.value.jsonData.recipients.join(), // 接收方,多个逗号隔开
                };
            } else if (formData.value.trigger == 'Once result recognized') {
                //console.log(formData.value.channels);
                const abilityIds = [],
                    deviceIds = [];
                formData.value.channels?.map(item => {
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
            const params = {
                name: formData.value.name,
                trigger: formData.value.trigger,
                actions: formData.value.actions.mjoin(),
                jsonData,
            };
            await addRule(params);
            emit('updateTable');
            resetForm(formEl);
            message($t('status.success'));
        }
    });
};

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};

const closeDialog = () => {
    formVisible.value = false;
    resetForm(ruleFormRef.value);
};

watch(
    () => formVisible.value,
    val => {
        if (val) {
            setTimeout(() => {
                ruleFormRef.value.clearValidate(['jsonData.recipients']);
            }, 100);
        }
        emit('update:visible', val);
    },
);

watch(
    () => props.visible,
    val => {
        formVisible.value = val;
        if (val) {
            initRoiChannels();
        }
    },
);

watch(
    () => props.data,
    val => {
        formData.value = val;
    },
);
</script>

<template>
    <el-dialog
        v-model="formVisible"
        :title="$t('rules.addNewRule')"
        :width="590"
        draggable
        :before-close="closeDialog"
        :close-on-click-modal="false"
    >
        <!-- 表单内容 -->
        <el-form
            ref="ruleFormRef"
            :model="formData"
            :rules="rules"
            label-position="top"
            require-asterisk-position="right"
        >
            <el-form-item :label="$t('common.name')" prop="name">
                <el-input
                    v-model="formData.name"
                    :placeholder="$t('rules.pleaseEnterRuleName')"
                    maxlength="32"
                    clearable
                />
            </el-form-item>
            <el-form-item :label="$t('rules.trigger')" prop="trigger">
                <el-select v-model="formData.trigger" @change="changeTrigger">
                    <el-option
                        v-for="item in triggerOpts"
                        :key="item.value"
                        :label="$t(item.label)"
                        :value="item.text"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                :label="$t('rules.srcSensingObjects')"
                prop="sensingObjectIds"
                v-show="formData.trigger == 'Once data received'"
            >
                <el-select
                    v-show="formVisible"
                    v-model="formData.jsonData.sensingObjectIds"
                    multiple
                    filterable
                    clearable
                    :placeholder="$t('rules.pleaseSelChannels')"
                    @click="getObjectList()"
                    remote
                    :remote-method="getFilterObject"
                    @change="changeObject"
                >
                    <el-option v-for="item in objectList" :key="item.value" :label="item.name" :value="item.id" />
                </el-select>
            </el-form-item>
            <!-- Show Threshold when seleting Low battery-->
            <el-form-item
                prop="jsonData.threshold"
                v-if="formData.trigger == 'Low battery'"
                :label="$t('rules.threshold')"
            >
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

            <el-form-item
                :label="$t('rules.srcDevice')"
                v-show="formData.trigger !== 'Once data received' && formData.trigger !== 'Once result recognized'"
            >
                <el-select
                    v-model="formData.jsonData.deviceIds"
                    value-key="id"
                    multiple
                    filterable
                    clearable
                    :placeholder="$t('rules.anyDevices')"
                    @click="getDeviceList()"
                    :loading="loadingOpts"
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
                    v-model="formData.channels"
                    :options="casOptions"
                    :props="casProps"
                    popper-class="cas-popper"
                    clearable
                    filterable
                    :filter-method="handleFilterMethod"
                    :before-filter="filterMethod"
                    @change="casChange"
                    :style="{ width: '550px' }"
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
                v-if="formData.trigger == 'Once data received' || formData.trigger == 'Once result recognized'"
                :label="$t('rules.actions')"
            >
                <el-select v-model="formData.actions" disabled>
                    <el-option
                        v-for="item in actionOpts"
                        :key="item.value"
                        :label="$t(item.label)"
                        :value="item.text"
                    />
                </el-select>
            </el-form-item>
            <el-form-item v-else prop="actions" :label="$t('rules.actions')">
                <el-select
                    v-model="formData.actions"
                    multiple
                    :placeholder="$t('rules.pleaseSelAction')"
                    @change="changeActions"
                >
                    <el-option
                        v-for="item in actionOpts"
                        :key="item.value"
                        :label="$t(item.label)"
                        :value="item.text"
                    />
                </el-select>
            </el-form-item>
            <!-- 当执行动作有Send to recipients时，下方就会出现接收方配置 -->
            <el-form-item
                v-show="formData.actions == 'Send to recipients' || formData.actions?.includes('Send to recipients')"
                prop="jsonData.recipients"
                :label="$t('menus.recipients')"
            >
                <el-select
                    v-model="formData.jsonData.recipients"
                    multiple
                    clearable
                    filterable
                    :placeholder="$t('rules.pleaseSelRecipients')"
                    @click="getRecipientsList()"
                    remote
                    :remote-method="getFilterRecipient"
                    @change="changeRecipient()"
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
        <template #footer>
            <el-button @click="closeDialog" class="ms-cancel-btn">{{ $t('buttons.cancel') }}</el-button>
            <el-button type="primary" @click="submitForm(ruleFormRef)" class="exter-btn">
                {{ $t('buttons.save') }}
            </el-button>
        </template>
    </el-dialog>
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
