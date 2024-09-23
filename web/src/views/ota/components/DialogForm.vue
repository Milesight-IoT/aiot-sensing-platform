<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance, UploadProps, genFileId } from 'element-plus';
import { getDeviceProfileList } from '@/api/device';
import { addOtaPackage, addOtaPackageFile, getOtaByTitle, deleteOtaPackage } from '@/api/ota';
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
const ruleFormRef = ref<FormInstance>();
const formVisible = ref(false);
const formData = ref({
    type: 'FIRMWARE',
});
const deviceProfileList = ref([]);
async function searchDeviceType() {
    if (deviceProfileList.value.length) return;
    const queryParams = {
        pageSize: 10,
        page: 0,
        sortProperty: 'name',
        sortOrder: 'asc',
    };
    const { data } = await getDeviceProfileList(queryParams);
    deviceProfileList.value = data;
}
const fileUpload = ref<UploadInstance>();
const fileList = ref();
const disableSave = ref(true);
const beforeFileUpload: UploadProps['beforeUpload'] = rawFile => {
    if (rawFile.type !== 'image/jpeg') {
        message($t('ota.errorFileTips'), { type: 'error' });
        return false;
    }
    return true;
};
const handleFileChange: UploadProps['onChange'] = uploadFile => {
    let title = document.querySelector('.el-icon--document');
    title?.classList?.add('icon-version');
    title = null;
    const { size, status, name } = uploadFile;
    const pattern = /^[a-zA-Z0-9.()_\-\s]*$/;
    console.log(size / 1024 / 1024);
    if (size / 1024 / 1024 > 200) {
        fileUpload.value!.clearFiles();
        return message($t('ota.sizeLimit'), { type: 'error' });
    }
    if (!pattern.test(name)) {
        fileUpload.value!.clearFiles();
        return message($t('ota.fileNameLimit'), { type: 'error' });
    }
    if (status == 'ready') disableSave.value = false;
    console.log('file upload success');
};
const handleRemoveFile: UploadProps['onRemove'] = (uploadFile, uploadFiles) => {
    const files = fileList.value;
    if (!files[0]) disableSave.value = true;
};
const limitOneFile: UploadProps['onExceed'] = files => {
    fileUpload.value!.clearFiles();
    const file = files[0] as UploadRawFile;
    file.uid = genFileId();
    fileUpload.value!.handleStart(file);
};
let otaPackageId = '';
const showLoading = ref(false);
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    try {
        showLoading.value = true;
        await formEl.validate();
        const files = fileList.value;
        if (files && files[0]) {
            const title = files[0].name;
            const params = { ...formData.value, title };
            const { id } = await getOtaByTitle(title);
            if (id) {
                message($t('ota.packageExist'), { type: 'error' });
                showLoading.value = false;
            } else {
                const otaInfo = await addOtaPackage(params);
                otaPackageId = otaInfo.id?.id;
                fileUpload.value!.submit();
            }
        } else {
            showLoading.value = false;
            message($t('ota.pleaseUploadPkg'), { type: 'error' });
        }
    } catch (e) {}
};
const allUpload = async ({ file }) => {
    try {
        const fileData = new FormData();
        fileData.append('file', file);
        disableSave.value = true;
        await addOtaPackageFile(fileData, otaPackageId);
        otaPackageId = '';
        emit('updateTable');
        message($t('status.success'));
        closeDialog();
    } catch (e) {
        await deleteOtaPackage(otaPackageId);
        otaPackageId = '';
        message($t('error.netError'), { type: 'error' });
        closeDialog();
    }
};

const resetForm = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
};

const closeDialog = async () => {
    if (otaPackageId && showLoading.value) await deleteOtaPackage(otaPackageId);
    formVisible.value = false;
    showLoading.value = false;
    disableSave.value = true;
    resetForm(ruleFormRef.value);
    fileUpload.value!.clearFiles();
};

const emit = defineEmits(['update:visible', 'updateTable']);
watch(
    () => formVisible.value,
    val => {
        emit('update:visible', val);
    },
);

watch(
    () => props.visible,
    val => {
        formVisible.value = val;
    },
);

watch(
    () => props.data,
    val => {
        formData.value = val;
    },
);

const rules = {
    type: [
        {
            required: true,
            message: $t('common.requireFailed'),
            trigger: 'change',
        },
    ],
    deviceProfileId: {
        required: true,
        message: $t('common.requireFailed'),
        trigger: 'blur',
    },
};
</script>

<template>
    <el-dialog
        v-model="formVisible"
        :title="$t('ota.addPackage')"
        :width="590"
        draggable
        :before-close="closeDialog"
        :close-on-click-modal="false"
    >
        <!-- 上传控件 -->
        <el-upload
            ref="fileUpload"
            class="upload-demo"
            v-model:file-list="fileList"
            :http-request="allUpload"
            drag
            :limit="1"
            :on-change="handleFileChange"
            :auto-upload="false"
            :on-exceed="limitOneFile"
            :on-remove="handleRemoveFile"
        >
            <i class="icon-upgrade" />
            <div class="el-upload__text">
                {{ $t('ota.dropFile') }} <em>{{ $t('ota.click2Upload') }}</em>
            </div>
        </el-upload>
        <!-- 表单内容 -->
        <el-form
            ref="ruleFormRef"
            :model="formData"
            :rules="rules"
            label-position="top"
            require-asterisk-position="right"
            class="flex flex-wrap justify-between mt-3 items-end"
        >
            <el-form-item :label="$t('common.type')" :style="{ width: '265px' }" prop="type">
                <el-select v-model="formData.type">
                    <el-option :label="$t('ota.firmware')" value="FIRMWARE" />
                    <el-option :label="$t('ota.configFile')" value="CONFIGURE" />
                </el-select>
            </el-form-item>
            <el-form-item :label="$t('device.model')" :style="{ width: '265px' }" prop="deviceProfileId">
                <el-select
                    v-model="formData.deviceProfileId"
                    value-key="id"
                    clearable
                    filterable
                    :placeholder="$t('ota.pleaseSelectModel')"
                    @click="searchDeviceType"
                >
                    <el-option
                        v-for="item in deviceProfileList"
                        :key="item.value"
                        :label="item.name"
                        :value="item.id"
                    />
                </el-select>
            </el-form-item>
            <el-form-item :style="{ width: '265px' }">
                <el-checkbox v-model="formData.distributeStatus" :label="$t('ota.distribute2all')" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="closeDialog" class="ms-cencel-btn">{{ $t('buttons.cancel') }}</el-button>
            <el-button type="primary" @click="submitForm(ruleFormRef)" class="exter-btn" :disabled="disableSave">
                {{ $t('buttons.save') }}
            </el-button>
        </template>
        <div class="loading-progress" v-if="showLoading" />
    </el-dialog>
</template>
<style lang="scss" scoped>
.icon-upgrade {
    font-size: 100px;
}
.loading-progress {
    height: 2px;
    position: absolute;
    top: 0;
    left: 0;
    background: #4fb6ff;
    overflow: hidden;
    animation: loading-progress 6s linear infinite;
}
@keyframes loading-progress {
    0% {
        width: 0%;
    }
    to {
        width: 100%;
    }
}
</style>
