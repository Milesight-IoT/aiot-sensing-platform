<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { addDevice } from '@/api/device';
import { getOtaPackagesBydeviceProfileId, getOtaByDeviceId } from '@/api/ota';

const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {
                type: '',
            };
        },
    },
});
const ruleFormRef = ref<FormInstance>();
const formData = ref({ ...props.data });
const otaList = ref([]);
const cfgList = ref([]);
async function searchOtaCfgList(type = 'FIRMWARE', isNew) {
    const isFirmware = type === 'FIRMWARE';
    if (!isNew && isFirmware && otaList.value.length) return;
    if (!isNew && !isFirmware && cfgList.value.length) return;
    const queryParams = {
        pageSize: 30,
        page: 0,
        sortProperty: 'fileName',
        sortOrder: 'asc',
    };
    const deviceProfileId = formData.value?.deviceProfileId.id;
    const { data } = await getOtaPackagesBydeviceProfileId(deviceProfileId, type, queryParams);
    isFirmware ? (otaList.value = data) : (cfgList.value = data);
}
const emit = defineEmits(['updateForm']);

const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    console.log(formData.value);
    const res = await formEl.validate();
    if (res) {
        try {
            const params = { ...formData.value };
            if (!params.firmwareId) params.firmwareId = null;
            if (!params.configureId) params.configureId = null;
            await addDevice(params, params.credentialsId);
            message($t('status.success'), { type: 'success' });
            emit('updateForm', params);
            resetForm(formEl);
        } catch (e) {}
    }
    console.log(res);
};
const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};
watch(
    () => props.data,
    val => {
        formData.value = val;
        initForam();
    },
);
const initForam = async () => {
    await searchOtaCfgList('FIRMWARE', true);
    await searchOtaCfgList('CONFIGURE', true);
    onGetOtaByDeviceId();
};

const otaStatus = ref({ text: '', class: 'text-succeess' });
const cfgStatus = ref({ text: '', class: 'text-succeess' });
const onGetOtaByDeviceId = async () => {
    const deviceId = formData.value.id.id;
    const { firmwareId, configureId } = formData.value;
    if (firmwareId || configureId) {
        const { fwTitle, fwChecksum, cfTitle, cfChecksum } = await getOtaByDeviceId(deviceId);
        if (!firmwareId) otaStatus.value.text = '';
        else {
            const otaInfo = otaList.value.filter(item => item.id.id == firmwareId?.id)[0];
            if (fwTitle == otaInfo?.title && fwChecksum == otaInfo.checksum)
                otaStatus.value = {
                    text: $t('status.success'),
                    class: 'text-success',
                };
            else
                otaStatus.value = {
                    text: $t('status.destributing'),
                    class: 'text-waring',
                };
        }
        if (!configureId) cfgStatus.value.text = '';
        else {
            const cfgInfo = cfgList.value.filter(item => item.id.id == configureId?.id)[0];
            if (cfTitle == cfgInfo?.title && cfChecksum == cfgInfo.checksum)
                cfgStatus.value = {
                    text: $t('status.success'),
                    class: 'text-success',
                };
            else
                cfgStatus.value = {
                    text: $t('status.destributing'),
                    class: 'text-waring',
                };
        }
    } else {
        otaStatus.value.text = '';
        cfgStatus.value.text = '';
    }
};
onMounted(() => {
    initForam();
});
</script>

<template>
    <el-form ref="ruleFormRef" :model="formData" label-position="top" require-asterisk-position="right">
        <el-form-item :label="$t('ota.firmware')">
            <el-select
                v-model="formData.firmwareId"
                value-key="id"
                clearable
                filterable
                :placeholder="$t('ota.pleaseSelectOta')"
                @click="searchOtaCfgList('FIRMWARE')"
                :error="otaStatus.text"
            >
                <el-option v-for="item in otaList" :key="item.value" :label="item.title" :value="item.id" />
            </el-select>
            <span :class="otaStatus.class">{{ otaStatus.text }}</span>
        </el-form-item>
        <el-form-item :label="$t('ota.configFile')">
            <el-select
                v-model="formData.configureId"
                value-key="id"
                clearable
                filterable
                :placeholder="$t('ota.pleaseSelectConfig')"
                @click="searchOtaCfgList('CONFIGURE')"
            >
                <el-option v-for="item in cfgList" :key="item.value" :label="item.title" :value="item.id" />
            </el-select>
            <span :class="cfgStatus.class">{{ cfgStatus.text }}</span>
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="submitForm(ruleFormRef)" class="float-right exter-btn">
        {{ $t('buttons.save') }}
    </el-button>
</template>
