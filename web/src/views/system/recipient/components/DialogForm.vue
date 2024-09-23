<script setup lang="ts">
import { ref, watch } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { getRecipientByName, addRecipient } from '@/api/recipient';
import { checkPort } from '@/utils/rule';
import { protocolOpts } from './data';
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
const formRef = ref<FormInstance>();
const formVisible = ref(false);
const formData = ref(props.data);
const requiredRule = {
    required: true,
    trigger: 'blur',
    message: () => $t('common.requireFailed'),
};
const rules = {
    name: [
        {
            required: true,
            trigger: 'blur',
            validator: async (rule, value, callback) => {
                const textSearch = value.trim();
                if (textSearch) {
                    const { name } = await getRecipientByName(textSearch);
                    if (!name) {
                        callback();
                    } else {
                        callback($t('recip.nameExist'));
                    }
                } else {
                    callback($t('common.requireFailed'));
                }
            },
        },
    ],
    'jsonData.url': requiredRule,
    'jsonData.host': requiredRule,
    'jsonData.port': [
        requiredRule,
        {
            validator: checkPort,
        },
    ],
    'jsonData.topic': requiredRule,
};

/**
 * 切换通讯协议时要求清空除了Name之外的数据
 */
function changeProtocol() {
    formRef.value.resetFields(['username', 'password']);
    formData.value.jsonData = {};
}

const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    await formEl.validate(async valid => {
        if (valid) {
            formVisible.value = false;
            const params = { ...formData.value };
            await addRecipient(params);
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
    resetForm(formRef.value);
};

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
</script>

<template>
    <el-dialog
        v-model="formVisible"
        :title="$t('recip.addNewRecipient')"
        :width="590"
        draggable
        :before-close="closeDialog"
        :close-on-click-modal="false"
        style="--el-dialog-margin-top: 5vh"
    >
        <!-- 表单内容 -->
        <el-form ref="formRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
            <el-form-item prop="name" :label="$t('common.name')">
                <el-input v-model="formData.name" :placeholder="$t('recip.pleaseEnterName')" maxlength="32" clearable />
            </el-form-item>
            <el-form-item :label="$t('recip.transProtocol')">
                <el-select v-model="formData.transmissionType" @change="changeProtocol">
                    <el-option
                        v-for="item in protocolOpts"
                        :key="item.value"
                        :label="$t(item.label)"
                        :value="item.text"
                    />
                </el-select>
            </el-form-item>
            <!-- HTTP -->
            <div v-if="formData.transmissionType == 'HTTP Post'">
                <el-form-item prop="jsonData.url" :label="$t('recip.url')">
                    <el-input
                        v-model="formData.jsonData.url"
                        placeholder="http://example.com/httpevent"
                        maxlength="256"
                    />
                </el-form-item>
            </div>
            <!-- MQTT -->
            <div v-if="formData.transmissionType == 'MQTT'">
                <el-form-item prop="jsonData.host" :label="$t('recip.host')">
                    <el-input v-model="formData.jsonData.host" placeholder="example.com" maxlength="256" />
                </el-form-item>
                <el-form-item prop="jsonData.port" :label="$t('recip.port')">
                    <el-input
                        v-model="formData.jsonData.port"
                        v-input-limit:number
                        maxlength="5"
                        :placeholder="$t('recip.pleaseEnterHost')"
                    />
                </el-form-item>
                <el-form-item prop="jsonData.topic" :label="$t('recip.topic')">
                    <!-- TODO v-input-limit 限制输入数字、英文和英文符号 -->
                    <el-input
                        v-model="formData.jsonData.topic"
                        v-input-limit:lettersymbol
                        maxlength="64"
                        :placeholder="$t('recip.pleaseEnterTopic')"
                    />
                </el-form-item>
            </div>
            <el-form-item :label="$t('common.username')" prop="username">
                <el-input v-model="formData.username" maxlength="64" />
            </el-form-item>
            <el-form-item :label="$t('common.password')" prop="password">
                <el-input v-model="formData.password" type="password" show-password maxlength="64" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="closeDialog" class="ms-cancel-btn">{{ $t('buttons.cancel') }}</el-button>
            <el-button type="primary" @click="submitForm(formRef)" class="exter-btn">
                {{ $t('buttons.save') }}
            </el-button>
        </template>
    </el-dialog>
</template>
