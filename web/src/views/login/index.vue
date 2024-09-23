<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ref, reactive, computed } from 'vue';
import { initRouter } from '@/router/utils';
import type { FormInstance, FormRules } from 'element-plus';
import { $t } from '@/plugins/i18n';
import { useLayout } from '@/layout/hooks/useLayout';
import { useUserStoreHook } from '@/store/modules/user';
import { message } from '@/utils/message';

defineOptions({
    name: 'Login',
});
const router = useRouter();
const loading = ref(false);
const loginFormRef = ref<FormInstance>();

const { initStorage } = useLayout();
initStorage();

const loginForm = reactive({
    username: '',
    password: '',
});

const rememberMe = ref<Boolean>(false);
const loginRules = reactive(<FormRules>{
    username: [
        {
            required: true,
            message: $t('common.requireFailed'),
            trigger: 'blur',
        },
    ],
    password: [
        {
            required: true,
            message: $t('common.requireFailed'),
            trigger: 'blur',
        },
    ],
});

/** 点击登录 */
const onLoginVerify = (formEl: FormInstance | undefined) => {
    if (!formEl) return;
    // 1.进行表单校验
    formEl.validate((valid, fields) => {
        if (valid) {
            onLogin(loginFormRef.value);
        } else {
            return fields;
        }
    });
};
const defaultId = computed(() => {
    return useUserStoreHook()?.defaultId;
});
const onLogin = async (formEl: FormInstance | undefined) => {
    loading.value = true;
    if (!formEl) return;
    const data = await useUserStoreHook().loginByUsername({ ...loginForm });

    if (data.token) {
        initRouter().then(() => {
            // 登录后直接跳转至默认仪表盘
            console.log(defaultId.value);
            router.push(`/dashboards/${defaultId.value}`);
            // router.push('/');
            message($t('status.success'));
        });
    } else {
        message($t('status.loginFailed'), { type: 'error' });
        useUserStoreHook().logOut();
    }
    loading.value = false;
};
</script>

<template>
    <div class="login-background">
        <Motion :delay="100">
            <div class="login-main">
                <el-form
                    class="login-content"
                    ref="loginFormRef"
                    :model="loginForm"
                    :rules="loginRules"
                    @keyup.enter="onLoginVerify(loginFormRef)"
                >
                    <i class="icon-logo logo"></i>
                    <div class="device-type">AIoT Sensing Platform</div>
                    <el-form-item prop="username">
                        <el-input clearable v-model="loginForm.username" :placeholder="$t('common.username')">
                            <template #prefix>
                                <i class="text-2xl icon-name"></i>
                            </template>
                        </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input
                            clearable
                            show-password
                            v-model="loginForm.password"
                            :placeholder="$t('common.password')"
                        >
                            <template #prefix>
                                <i class="text-2xl icon-password"></i>
                            </template>
                        </el-input>
                    </el-form-item>
                    <el-form-item class="remember" v-if="false">
                        <el-checkbox v-model="rememberMe" @keypress.enter.native="onLoginVerify">{{
                            $t('rememberMe')
                        }}</el-checkbox>
                        <el-button type="text" @click="resetPwd">{{ $t('forgetPwd') }}</el-button>
                        <el-button v-show="false" id="tpmBtn"></el-button>
                    </el-form-item>
                    <div class="button">
                        <el-button
                            class="w-full login-btn"
                            size="default"
                            type="primary"
                            :loading="loading"
                            @click="onLoginVerify(loginFormRef)"
                        >
                            {{ $t('auth.login') }}
                        </el-button>
                    </div>
                </el-form>
            </div>
        </Motion>
    </div>
</template>
<style lang="scss" scoped>
@import '@/style/login.scss';
</style>
