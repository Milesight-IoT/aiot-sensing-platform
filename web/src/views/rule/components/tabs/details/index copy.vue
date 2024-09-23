<!--
 * @Author: sreindy8224 sreindy@milesight.com
 * @Date: 2023-04-24 10:07:42
 * @LastEditTime: 2023-07-10 21:11:01
 * @Descripttion: 规则右侧详情
-->

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { emitter } from '@/utils/mitt';
import { addRule, getRuleByName } from '@/api/rules';
import { getUserDeviceList, getDeviceListById } from '@/api/device';
import { getSensingObjectList, getSensingObjectById, getSensingObjectSelectList } from '@/api/sensing-object';
import { getRecipientList, getRecipientListById } from '@/api/recipient';
import { useRule } from '@/views/rule/list/hook';
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

const { triggerOpts, actionOpts } = useRule();

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
const deviceList = ref([]);
let filterDeviceListTmp = [];
const loadingOpts = ref(true);
const devicePageArr = [];
let deviceQuery = {
    currentpage: 0,
    hasNextPage: true,
    totalElements: 0,
};
const getDeviceList = async (
    queryParams = {
        pageSize: 100,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'ASC',
    },
) => {
    if (deviceQuery.totalElements && queryParams.page < deviceQuery.currentpage) return;
    let tmpData = [];
    const { data, totalElements, hasNext } = await getUserDeviceList(queryParams);
    deviceQuery = {
        hasNextPage: hasNext,
        currentpage: queryParams.page + 1,
        totalElements,
    };
    const deviceIds = formData.value.jsonData?.deviceIds || [];
    const newData = data.filter(item => {
        return !deviceIds.includes(item.id.id);
    });

    tmpData = deviceList.value.concat(newData);
    filterDeviceListTmp = tmpData;
    deviceList.value = tmpData;
    loadingOpts.value = false;
};
const getSelDeviceList = async deviceIds => {
    const params = {
        deviceIds,
    };
    const tmpData = await getDeviceListById(params);
    deviceQuery = {
        hasNextPage: true,
        currentpage: 0,
        totalElements: 0,
    };
    deviceList.value = tmpData;
    filterDeviceListTmp = tmpData;
    loadingOpts.value = false;
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
    } else {
        deviceList.value = filterDeviceListTmp;
    }
};

const objectList = ref([]);
let filterObjectListTmp = [];
let objectQuery = {
    currentPage: 0,
    hasNextPage: true,
    totalElements: 0,
};
const getObjectList = async (
    queryParams = {
        pageSize: 100,
        page: 0,
        needChannels: true,
    },
) => {
    if (objectQuery.totalElements && queryParams.page < objectQuery.currentPage) return;
    let tmpData = [];
    const { data, hasNext, totalElements } = await getSensingObjectSelectList(queryParams);
    objectQuery = {
        hasNextPage: hasNext,
        currentPage: queryParams.page + 1,
        totalElements,
    };
    console.log(objectQuery);
    if (queryParams.page) {
        const sensingObjectIds = formData.value.jsonData?.sensingObjectIds || [];
        const newData = data.filter(item => {
            return !sensingObjectIds.includes(item.id.id);
        });
        tmpData = objectList.value.concat(newData);
    } else {
        tmpData = data;
    }
    objectList.value = tmpData;
    filterObjectListTmp = tmpData;
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
    loadingOpts.value = false;
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
    } else {
        objectList.value = filterObjectListTmp;
    }
};
const recipientList = ref([]);
let filterRecipientListTmp = [];
let recipientQuery = { currentPage: 0, hasNextPage: true, totalElements: 0 };
const getRecipientsList = async (
    queryParams = {
        pageSize: 100,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'ASC',
    },
) => {
    if (recipientQuery.totalElements && typeof queryParams.page != 'number') return;
    let tmpData = [];
    const { data, hasNext, totalElements } = await getRecipientList(queryParams);
    recipientQuery = {
        hasNextPage: hasNext,
        currentPage: queryParams.page + 1,
        totalElements,
    };
    if (queryParams.page) {
        const recipients = formData.value.jsonData?.recipients || [];
        const newData = data.filter(item => {
            return !recipients.includes(item.id.id);
        });
        tmpData = recipientList.value.concat(newData);
    } else {
        tmpData = data;
    }
    filterRecipientListTmp = tmpData;
    recipientList.value = tmpData;
    //loadingOpts.value = false;
};
const getSelRecipientList = async recipientsIds => {
    const params = {
        recipientsIds,
    };
    const tmpData = await getRecipientListById(params);
    recipientList.value = tmpData;
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
        recipientList.value = data;
    } else {
        recipientList.value = filterRecipientListTmp;
    }
};
const loadMoreOpts = () => {
    const trigger = formData.value.trigger;
    if (trigger == 'Once data received') {
        const { hasNextPage, currentPage } = objectQuery;
        console.log(currentPage);
        if (hasNextPage)
            getObjectList({ pageSize: 100, page: currentPage || 0, sortProperty: 'name', sortOrder: 'ASC' });
    } else {
        const { hasNextPage, currentPage } = deviceQuery;
        if (hasNextPage)
            getDeviceList({ pageSize: 100, page: currentPage || 0, sortProperty: 'name', sortOrder: 'ASC' });
    }
};
const handleScrollRecipents = () => {
    const { hasNextPage, currentPage } = recipientQuery;
    if (hasNextPage)
        getRecipientsList({ pageSize: 100, page: currentPage || 0, sortProperty: 'name', sortOrder: 'ASC' });
};

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
                    sensingObjectIds: formData.value.jsonData.sensingObjectIds.mjoin(), //设备ID 多个逗号隔开
                };
            } else if (formData.value.trigger == 'Low battery') {
                jsonData = {
                    threshold: formData.value.jsonData.threshold, // 电量
                    deviceIds: formData.value.jsonData.deviceIds.mjoin(),
                };
            } else if (formData.value.trigger == 'Devices become inactive') {
                jsonData = {
                    deviceIds: formData.value.jsonData.deviceIds.mjoin(), //设备ID 多个逗号隔开
                };
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
            >
                <ul class="infinite-list" v-infinite-scroll="loadMoreOpts" style="overflow: auto">
                    <el-option v-for="item in objectList" :key="item.value" :label="item.name" :value="item.id" />
                </ul>
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
            >
                <!-- <ul class="infinite-list" v-infinite-scroll="loadMoreOpts" style="overflow: auto"> -->
                <el-option v-for="item in deviceList" :key="item.value" :label="item.name" :value="item.id.id" />
                <!-- </ul> -->
            </el-select>
        </el-form-item>
        <!-- 三种trigger模式下都有action，当选择Once data received时action固定选择send to禁用 -->
        <el-form-item :label="$t('rules.actions')" v-if="formData.trigger == 'Once data received'">
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
                :placeholder="$t('rules.pleaseSelRecipients')"
                @click="getRecipientsList()"
                remote
                :remote-method="getFilterRecipient"
            >
                <ul class="infinite-list" v-infinite-scroll="handleScrollRecipents" style="overflow: auto">
                    <el-option
                        v-for="item in recipientList"
                        :key="item.value"
                        :label="item.name"
                        :value="item.recipientsId.id"
                    />
                </ul>
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
