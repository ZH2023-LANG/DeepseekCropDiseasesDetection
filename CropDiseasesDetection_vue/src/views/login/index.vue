<template>
	<div class="login-container">
		<div class="bg-bubbles">
			<li v-for="n in 10" :key="n"></li>
		</div>

		<div class="login-box animate__animated animate__fadeIn">
			<div class="title">
				<h2>基于YOLO+DeepSeek的农作物病虫害检测系统</h2>
				<p>Crop Disease and Insect Pests Detection System Based on YOLO+DeepSeek</p>
			</div>

			<el-form :model="ruleForm" :rules="registerRules" ref="ruleFormRef">
				<el-form-item prop="username">
					<el-input v-model="ruleForm.username" placeholder="请输入用户名" prefix-icon="User" class="custom-input" />
				</el-form-item>

				<el-form-item prop="password">
					<el-input v-model="ruleForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password class="custom-input" />
				</el-form-item>

				<el-form-item>
					<el-button type="primary" class="login-btn" @click="submitForm(ruleFormRef)"> 登录 </el-button>
				</el-form-item>
			</el-form>

			<div class="options">
				<router-link to="/register">注册账号</router-link>
				<span>|</span>
				<a href="#">忘记密码</a>
			</div>
		</div>
	</div>
</template>

<script lang="ts" setup>
import { reactive, computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import Cookies from 'js-cookie';
import { storeToRefs } from 'pinia';
import { useThemeConfig } from '/@/stores/themeConfig';
import { initFrontEndControlRoutes } from '/@/router/frontEnd';
import { initBackEndControlRoutes } from '/@/router/backEnd';
import { Session } from '/@/utils/storage';
import { formatAxis } from '/@/utils/formatTime';
import { NextLoading } from '/@/utils/loading';
import type { FormInstance, FormRules } from 'element-plus';
import request from '/@/utils/request';

// 定义变量内容
const { t } = useI18n();
const storesThemeConfig = useThemeConfig();
const { themeConfig } = storeToRefs(storesThemeConfig);
const route = useRoute();
const router = useRouter();
const formSize = ref('default');
const ruleFormRef = ref<FormInstance>();

const ruleForm = reactive({
	username: '',
	password: '',
});

const registerRules = reactive<FormRules>({
	username: [
		{ required: true, message: '请输入账号', trigger: 'blur' },
		{ min: 3, max: 5, message: '长度在3-5个字符', trigger: 'blur' },
	],
	password: [
		{ required: true, message: '请输入密码', trigger: 'blur' },
		{ min: 3, max: 5, message: '长度在3-5个字符', trigger: 'blur' },
	],
});

const currentTime = computed(() => {
	return formatAxis(new Date());
});

const onSignIn = async () => {
	Session.set('token', Math.random().toString(36).substr(0));
	Cookies.set('userName', ruleForm.username);
	if (!themeConfig.value.isRequestRoutes) {
		const isNoPower = await initFrontEndControlRoutes();
		signInSuccess(isNoPower);
	} else {
		const isNoPower = await initBackEndControlRoutes();
		signInSuccess(isNoPower);
	}
};

const signInSuccess = (isNoPower: boolean | undefined) => {
	if (isNoPower) {
		ElMessage.warning('抱歉，您没有登录权限');
		Session.clear();
	} else {
		let currentTimeInfo = currentTime.value;
		if (route.query?.redirect) {
			router.push({
				path: <string>route.query?.redirect,
				query: Object.keys(<string>route.query?.params).length > 0 ? JSON.parse(<string>route.query?.params) : '',
			});
		} else {
			router.push('/');
		}
		const signInText = t('message.signInText');
		ElMessage.success(`${currentTimeInfo}，${signInText}`);
		NextLoading.start();
	}
};

const submitForm = (formEl: FormInstance | undefined) => {
	if (!formEl) return;
	formEl.validate((valid) => {
		if (valid) {
			request.post('/api/user/login', ruleForm).then((res) => {
				if (res.code == 0) {
					Cookies.set('role', res.data.role);
					onSignIn();
				} else {
					ElMessage({
						type: 'error',
						message: res.msg,
					});
				}
			});
		} else {
			console.log('error submit!');
			return false;
		}
	});
};
</script>

<style scoped>
.login-container {
	min-height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #C8E6C9 0%, #4CAF50 100%); /* 绿色系渐变 */
	padding: 20px;
}

.title {
	text-align: center;
	margin-bottom: 35px;
}

.title h2 {
	font-size: 18px;
	color: #2E7D32; /* 深绿色标题 */
	margin-bottom: 10px;
	font-weight: 600;
}

.title p {
	font-size: 14px;
	color: #81C784; /* 浅绿色副标题 */
	letter-spacing: 1px;
}

:deep(.custom-input .el-input__wrapper) {
	box-shadow: 0 2px 8px rgba(76, 175, 80, 0.1); /* 绿色阴影 */
	border-radius: 8px;
	padding: 12px 15px;
	background: #E8F5E9; /* 浅绿色背景 */
}

:deep(.custom-input .el-input__wrapper:hover) {
	box-shadow: 0 2px 12px rgba(76, 175, 80, 0.2);
}

:deep(.custom-input .el-input__wrapper.is-focus) {
	box-shadow: 0 0 0 1px #4CAF50; /* 鲜绿色焦点边框 */
	background: #F1F8E9;
}

:deep(.custom-input .el-input__prefix .el-icon) {
	color: #4CAF50; /* 鲜绿色图标 */
}

.login-btn {
	width: 100%;
	padding: 12px 0;
	font-size: 16px;
	font-weight: 500;
	letter-spacing: 1px;
	border-radius: 8px;
	background: #4CAF50; /* 鲜绿色按钮 */
	border: none;
	margin-top: 10px;
	transition: transform 0.3s ease;
}

.login-btn:hover {
	transform: translateY(-2px);
	background: #388E3C; /* 稍深绿色 */
	opacity: 0.9;
}

.options {
	margin-top: 25px;
	text-align: center;
}

.options a {
	color: #4CAF50; /* 鲜绿色链接 */
	text-decoration: none;
	font-size: 15px;
	transition: all 0.3s ease;
	font-weight: 500;
}

.options span {
	color: #C8E6C9; /* 淡绿色分隔符 */
	margin: 0 15px;
}

.options a:hover {
	color: #81C784; /* 浅绿色悬浮 */
	text-decoration: underline;
}

/* 响应式适配 */
@media (max-width: 768px) {
	.login-box {
		width: 90%;
		padding: 30px 20px;
	}

	.title h2 {
		font-size: 24px;
	}

	.title p {
		font-size: 14px;
	}
}

/* 添加背景气泡动画 */
.bg-bubbles {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	z-index: 1;
	overflow: hidden;
}

.bg-bubbles li {
	position: absolute;
	list-style: none;
	display: block;
	width: 40px;
	height: 40px;
	background-color: rgba(76, 175, 80, 0.15); /* 绿色系气泡 */
	bottom: -160px;
	animation: square 25s infinite;
	transition-timing-function: linear;
}

.bg-bubbles li:nth-child(1) {
	left: 10%;
	width: 80px;
	height: 80px;
	animation-delay: 0s;
}

.bg-bubbles li:nth-child(2) {
	left: 20%;
	width: 90px;
	height: 90px;
	animation-delay: 2s;
	animation-duration: 17s;
}

.bg-bubbles li:nth-child(3) {
	left: 25%;
	animation-delay: 4s;
}

.bg-bubbles li:nth-child(4) {
	left: 40%;
	width: 60px;
	height: 60px;
	animation-duration: 22s;
}

.bg-bubbles li:nth-child(5) {
	left: 70%;
	width: 120px;
	height: 120px;
}

.bg-bubbles li:nth-child(6) {
	left: 80%;
	width: 90px;
	height: 90px;
	animation-delay: 3s;
}

.bg-bubbles li:nth-child(7) {
	left: 32%;
	width: 60px;
	height: 60px;
	animation-delay: 7s;
}

.bg-bubbles li:nth-child(8) {
	left: 55%;
	width: 20px;
	height: 20px;
	animation-delay: 15s;
	animation-duration: 40s;
}

.bg-bubbles li:nth-child(9) {
	left: 25%;
	width: 10px;
	height: 10px;
	animation-delay: 2s;
	animation-duration: 40s;
}

.bg-bubbles li:nth-child(10) {
	left: 90%;
	width: 160px;
	height: 160px;
	animation-delay: 11s;
}

@keyframes square {
	0% {
		transform: translateY(0) rotate(0deg);
		opacity: 1;
	}
	100% {
		transform: translateY(-1000px) rotate(600deg);
		opacity: 0;
	}
}

.login-box {
	position: relative;
	z-index: 2;
	transform: translateY(20px);
	animation: slideUp 0.8s forwards;
	opacity: 0;
	width: 480px;
	padding: 40px 40px;
	background: rgba(241, 248, 233, 0.95); /* 浅绿色半透明背景 */
	border-radius: 16px;
	box-shadow: 0 15px 35px rgba(76, 175, 80, 0.2); /* 绿色阴影 */
	backdrop-filter: blur(10px);
}

@keyframes slideUp {
	from {
		transform: translateY(20px);
		opacity: 0;
	}
	to {
		transform: translateY(0);
		opacity: 1;
	}
}

/* 修改输入框动画部分 */
:deep(.el-form-item) {
	opacity: 0;
}

:deep(.el-form-item:nth-child(odd)) {
	transform: translateX(-50px);
	animation: slideRightIn 0.5s forwards;
}

:deep(.el-form-item:nth-child(even)) {
	transform: translateX(50px);
	animation: slideLeftIn 0.5s forwards;
}

:deep(.el-form-item:nth-child(1)) {
	animation-delay: 0.2s;
}
:deep(.el-form-item:nth-child(2)) {
	animation-delay: 0.4s;
}

@keyframes slideRightIn {
	from {
		transform: translateX(-50px);
		opacity: 0;
	}
	to {
		transform: translateX(0);
		opacity: 1;
	}
}

@keyframes slideLeftIn {
	from {
		transform: translateX(50px);
		opacity: 0;
	}
	to {
		transform: translateX(0);
		opacity: 1;
	}
}

/* 按钮悬浮效果增强 */
.login-btn {
	transition: all 0.3s ease;
}

.login-btn:hover {
	transform: translateY(-3px);
	box-shadow: 0 7px 14px rgba(76, 175, 80, 0.2), 0 3px 6px rgba(76, 175, 80, 0.1); /* 绿色阴影 */
}

.login-btn:active {
	transform: translateY(-1px);
}

/* 输入框焦点动画 */
:deep(.el-input__wrapper.is-focus) {
	animation: pulse 0.3s ease-in-out;
}

@keyframes pulse {
	0% {
		transform: scale(1);
	}
	50% {
		transform: scale(1.02);
	}
	100% {
		transform: scale(1);
	}
}
</style>