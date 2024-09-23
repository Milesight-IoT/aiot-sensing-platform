<script setup lang="ts">
import { ref, watch } from "vue";
import { $t } from "@/plugins/i18n";
import { message } from "@/utils/message";
import { FormInstance } from "element-plus";
import { changePwd, checkPwdMatch } from "@/api/user";
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  data: {
    type: Object,
    default: () => {
      return {};
    }
  }
});

const ruleFormRef = ref<FormInstance>();
const formVisible = ref(false);
const formData = ref(props.data);
const tipClass = ref(["ul-disc", "ul-disc"]);
const emit = defineEmits(["update:visible", "logout"]);
watch(
  () => formVisible.value,
  val => {
    emit("update:visible", val);
  }
);

watch(
  () => props.visible,
  val => {
    formVisible.value = val;
  }
);

watch(
  () => props.data,
  val => {
    formData.value = val;
  }
);

const rules = {
  curPwd: [
    {
      required: true,
      trigger: "blur",
      validator: async (rule, value, callback) => {
        const pwdText = value.trim();
        if (pwdText) {
          const isValid = await checkPwdMatch(pwdText);
          isValid && callback();
        }
        callback($t("auth.incorrectPwd"));
      }
    }
  ],
  newPwd: [
    {
      required: true,
      validator: (rule, value, callback) => {
        let pattern =
          /(?!^\d+$)(?!^[A-Za-z]+$)(?!^[^A-Za-z0-9]+$)(?!^.*[\u4E00-\u9FA5].*$)^\S{8,32}$/;
        if (value.trim().length > 7) {
          tipClass.value[0] = "icon-selected";
        } else {
          tipClass.value[0] = "ul-disc";
        }
        if (pattern.test(value)) {
          tipClass.value = ["icon-selected", "icon-selected"];
          callback();
        } else {
          tipClass.value[1] = "ul-disc";
          callback($t("auth.invalidPwd"));
        }
      },
      trigger: "blur"
    }
  ],
  confirmPwd: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (value.trim() != formData.value?.newPwd) {
          callback($t("auth.pwdDisMatch"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
};
const onChangePwd = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  await formEl.validate(async valid => {
    if (valid) {
      const { curPwd, newPwd } = formData.value;
      const params = {
        currentPassword: curPwd,
        newPassword: newPwd
      };
      try {
        await changePwd(params);
        emit("logout");
        resetForm(formEl);
        formVisible.value = false;
        message($t("status.success"));
      } catch (e) {
        console.log(e);
      }
    }
  });
};

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  tipClass.value = ["ul-disc", "ul-disc"];
  formEl.resetFields();
};

const closeDialog = () => {
  resetForm(ruleFormRef.value);
  formVisible.value = false;
};
</script>

<template>
  <el-dialog
    v-model="formVisible"
    :title="$t('auth.changePwd')"
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
      <el-form-item :label="$t('auth.curPwd')" prop="curPwd">
        <el-input
          clearable
          v-model="formData.curPwd"
          :placeholder="$t('auth.curPwd')"
          maxlength="32"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item :label="$t('auth.newPwd')" prop="newPwd">
        <el-input
          clearable
          v-model="formData.newPwd"
          :placeholder="$t('auth.newPwd')"
          maxlength="32"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item :label="$t('auth.confirmNewPwd')" prop="confirmPwd">
        <el-input
          clearable
          v-model="formData.confirmPwd"
          :placeholder="$t('auth.confirmNewPwd')"
          maxlength="32"
          type="password"
          show-password
        />
      </el-form-item>
    </el-form>
    <div>
      <h4>{{ $t("auth.atLeast") }}</h4>
      <ul class="px-5">
        <li :class="tipClass[0]">{{ $t("auth.pwdLimit1") }}</li>
        <li :class="tipClass[1]">{{ $t("auth.pwdLimit2") }}</li>
      </ul>
    </div>
    <template #footer>
      <el-button @click="closeDialog" class="ms-cancel-btn">{{
        $t("buttons.discard")
      }}</el-button>
      <el-button
        type="primary"
        @click="onChangePwd(ruleFormRef)"
        class="exter-btn"
      >
        {{ $t("auth.changePwd") }}
      </el-button>
    </template>
  </el-dialog>
</template>
<style lang="scss" scoped>
.ul-disc {
  list-style: disc;
}
.icon-selected {
  position: relative;
  font-size: 14px;
}
li.icon-selected::before {
  color: #24a148;
  position: absolute;
  left: -20px;
  font-size: 20px;
  line-height: 20px;
}
</style>
