<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { $t } from '@/plugins/i18n';
import { message } from '@/utils/message';
import { FormInstance } from 'element-plus';
import { emitter } from '@/utils/mitt';
import { getRecipientByName, addRecipient } from '@/api/recipient';
import { checkPort } from '@/utils/rule';
import { protocolOpts } from '../../data';
const props = defineProps({
    data: {
        type: Object,
        default: () => {
            return {};
        },
    },
});
const formRef = ref<FormInstance>();
const formData = ref({ ...props.data });

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
                    if (textSearch == props.data.name) callback();
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

const emit = defineEmits(['updateForm', 'updateTitle']);
const submitForm = async (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    const res = await formEl.validate();
    if (res) {
        const params = { ...formData.value };
        await addRecipient(params);
        emitter.emit('panelTitle', params.name);
        emit('updateForm', params);
        message($t('status.success'));
    }
};

watch(
    () => props.data,
    val => {
        formData.value = { ...val };
    },
);
</script>

<template>
    <el-form ref="formRef" :model="formData" :rules="rules" label-position="top" require-asterisk-position="right">
        <el-form-item prop="name" :label="$t('common.name')">
            <el-input v-model="formData.name" :placeholder="$t('recip.pleaseEnterName')" maxlength="32" clearable />
        </el-form-item>
        <el-form-item :label="$t('recip.transProtocol')">
            <el-select v-model="formData.transmissionType" @change="changeProtocol">
                <el-option v-for="item in protocolOpts" :key="item.value" :label="$t(item.label)" :value="item.text" />
            </el-select>
        </el-form-item>
        <!-- HTTP -->
        <div v-if="formData.transmissionType == 'HTTP Post'">
            <el-form-item prop="jsonData.url" :label="$t('recip.url')">
                <el-input v-model="formData.jsonData.url" placeholder="http://example.com/httpevent" maxlength="256" />
            </el-form-item>
        </div>
        <!-- MQTT -->
        <div v-if="formData.transmissionType == 'MQTT'">
            <el-form-item prop="jsonData.host" :label="$t('recip.host')">
                <el-input v-model="formData.jsonData.host" placeholder="example.com" maxlength="256" />
            </el-form-item>
            <el-form-item prop="jsonData.port" :label="$t('recip.port')" :placeholder="$t('recip.pleaseEnterHost')">
                <el-input v-model="formData.jsonData.port" v-input-limit:number maxlength="5" />
            </el-form-item>
            <el-form-item prop="jsonData.topic" :label="$t('recip.topic')" :placeholder="$t('recip.pleaseEnterTopic')">
                <!-- TODO v-input-limit 限制输入数字、英文和英文符号 -->
                <el-input v-model="formData.jsonData.topic" v-input-limit:lettersymbol maxlength="64" />
            </el-form-item>
        </div>
        <el-form-item :label="$t('common.username')">
            <el-input v-model="formData.username" maxlength="64" />
        </el-form-item>
        <el-form-item :label="$t('common.password')">
            <el-input v-model="formData.password" type="password" show-password maxlength="64" />
        </el-form-item>
    </el-form>
    <el-button type="primary" @click="submitForm(formRef)" class="float-right exter-btn">
        {{ $t('buttons.save') }}
    </el-button>
</template>
