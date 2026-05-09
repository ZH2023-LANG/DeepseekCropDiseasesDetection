<template>
	<div class="system-predict-container layout-padding" id="id" v-loading="state.loading">
		<div class="system-predict-padding layout-padding-auto layout-padding-view">
			<div class="carousel">
				<div class="section-title"><i></i><span>病害检测</span></div>
			</div>
			<div class="header">
				<div class="weight">
					<el-select v-model="weight" placeholder="请选择模型" size="large" style="width: 200px">
						<el-option v-for="item in state.weight_items" :key="item.value" :label="item.label"
							:value="item.value" />
					</el-select>
				</div>
				<div class="weight">
					<el-select v-model="ai" placeholder="请选择AI助手" size="large" style="margin-left: 20px;width: 200px"
						@change="getData">
						<el-option v-for="item in state.ai_items" :key="item.value" :label="item.label"
							:value="item.value" />
					</el-select>
				</div>
				<div class="conf" style="margin-left: 20px;display: flex; flex-direction: row;">
					<div
						style="font-size: 14px;margin-right: 20px;display: flex;justify-content: start;align-items: center;color: #2E7D32;">
						设置最小置信度阈值</div>
					<el-slider v-model="conf" :format-tooltip="formatTooltip" style="width: 300px;" />
				</div>
				<div class="button-section" style="margin-left: 20px">
					<el-button type="primary" @click="upData" class="predict-button">开始预测</el-button>
				</div>
				<div class="button-section" style="margin-left: 20px">
					<el-button type="primary" @click="() => htmlToPDF('id', '病害检测报告')"
						class="predict-button">PDF导出</el-button>
				</div>
			</div>
			<div style="width: 100%; height: 800px; display: flex; flex-direction: row;margin-bottom: 20px;">
				<el-card shadow="hover" class="card">
					<el-upload v-model="state.img" ref="uploadFile" class="avatar-uploader"
						action="http://localhost:9999/files/upload" :show-file-list="false"
						:on-success="handleAvatarSuccessone">
						<img v-if="imageUrl" :src="imageUrl" class="avatar" />
						<el-icon v-else class="avatar-uploader-icon">
							<Plus />
						</el-icon>
					</el-upload>
				</el-card>
				<el-card class="result-section" v-if="state.predictionResult.label">
					<div class="bottom">
						<div class="result-content">
							<el-card shadow="never" class="info-card">
								<div class="info-item">
									<div class="info-label">
										<el-icon class="icon"><Select /></el-icon>
										<span>识别结果</span>
									</div>
									<div class="info-value highlight">{{ state.predictionResult.label || '-' }}
									</div>
								</div>
								<div class="info-item">
									<div class="info-label">
										<el-icon class="icon">
											<Opportunity />
										</el-icon>
										<span>预测概率</span>
									</div>
									<div class="info-value accent">
										{{ state.predictionResult.confidence || '-' }}
									</div>
								</div>
								<div class="info-item">
									<div class="info-label">
										<el-icon class="icon">
											<Clock />
										</el-icon>
										<span>总耗时</span>
									</div>
									<div class="info-value">{{ state.predictionResult.allTime ?
										`${state.predictionResult.allTime}` : '-' }}</div>
								</div>
							</el-card>
						</div>
						<div style="width: 100%; margin-top: 20px;">
							<h4>详细结果</h4>
							<el-table :data="state.data" style="width: 100%">
								<el-table-column prop="label" label="预测结果" align="center" />
								<el-table-column prop="confidence" label="置信度" align="center" />
								<el-table-column prop="allTime" label="总用时" align="center" />
							</el-table>
						</div>
					</div>
				</el-card>
			</div>
			<div class="carousel">
				<div class="section-title"><i></i><span>AI建议</span></div>
			</div>
			<div style="width: 100%; margin-bottom: 20px; padding: 0 100px; display: flex; justify-content: center;">
				<div v-if="state.predictionResult.suggestion"
					style="width: 100%; max-width: 900px; padding: 20px 40px; border-radius: 10px; min-height: 50px; border: 1px solid #C8E6C9; background: #fff;">
					<div v-html="state.predictionResult.suggestion" class="markdown-body"></div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts" name="personal">
import { reactive, ref, onMounted } from 'vue';
import type { UploadInstance, UploadProps } from 'element-plus';
import { ElMessage } from 'element-plus';
import request from '/@/utils/request';
import { Loading, Plus } from '@element-plus/icons-vue';
import { useUserInfo } from '/@/stores/userInfo';
import { storeToRefs } from 'pinia';
import { formatDate } from '/@/utils/formatTime';
import { htmlToPDF } from '/@/utils/pdf';
import { marked } from "marked";
import { SocketService } from '/@/utils/socket';

const imageUrl = ref('');
const ai = ref('');
const conf = ref(50);
const weight = ref('');
const uploadFile = ref<UploadInstance>();
const stores = useUserInfo();
const { userInfos } = storeToRefs(stores);
const state = reactive({
	loading: false,
	weight_items: [] as any,
	img: '',
	data: [] as any,
	predictionResult: {
		label: '',
		confidence: '',
		allTime: '',
		suggestion: '' as any
	},
	ai_items: [
		{ value: 'DeepSeek', label: 'DeepSeek' },
		{ value: 'Qwen', label: 'Qwen' },
		{ value: '不使用AI', label: '不使用AI' },
	],
	form: {
		username: '',
		inputImg: null as any,
		weight: '',
		conf: null as any,
		ai: '',
		startTime: ''
	},
});

const formatTooltip = (val: number) => {
	return val / 100;
};

const socketService = new SocketService();

socketService.on('message', (data) => {
	console.log('Received message:', data);
	ElMessage({
		message: data,
		type: 'success',
		duration: 3000
	});
});

const handleAvatarSuccessone: UploadProps['onSuccess'] = (response, uploadFile) => {
	imageUrl.value = URL.createObjectURL(uploadFile.raw!);
	state.img = response.data;
};

const getData = () => {
	request.get('/api/flask/file_names').then((res) => {
		if (res.code == 0) {
			res.data = JSON.parse(res.data);
			state.weight_items = res.data.weight_items;
		} else {
			ElMessage.error(res.msg);
		}
	});
};

const transformData = (rawData: any): any => {
	return rawData.label.map((label, index) => ({
		allTime: rawData.allTime,
		confidence: rawData.confidence[index],
		label: label,
	}));
};

const upData = () => {
	state.loading = true;
	state.form.weight = weight.value;
	state.form.conf = parseFloat(conf.value.toString()) / 100,
		state.form.username = userInfos.value.userName;
	state.form.inputImg = state.img;
	state.form.ai = ai.value;
	state.form.startTime = formatDate(new Date(), 'YYYY-mm-dd HH:MM:SS');
	request.post('/api/flask/predict', state.form).then((res) => {
		if (res.code == 0) {
			try {
				state.loading = false;
				res.data = JSON.parse(res.data);
				state.predictionResult.label = JSON.parse(res.data.label);
				state.predictionResult.confidence = JSON.parse(res.data.confidence);
				state.predictionResult.allTime = res.data.allTime;
				state.predictionResult.suggestion = marked(res.data.suggestion);
				state.data = transformData(state.predictionResult);
				if (res.data.outImg) {
					imageUrl.value = res.data.outImg;
				} else {
					imageUrl.value = imageUrl.value;
				}
			} catch (error) {
				console.error('解析 JSON 时出错:', error);
			}
			ElMessage.success('预测成功！');
		} else {
			state.loading = false;
			ElMessage.error(res.msg);
		}
	});
};

onMounted(() => {
	getData();
});
</script>

<style scoped lang="scss">
.carousel {
	width: 100%;

	.section-title {
		margin-bottom: 50px;
		font-size: 20px;
		text-align: center;
		position: relative;
		padding: 20px 0;
		display: flex;
		justify-content: center;
		justify-items: center;

		i {
			background: #4CAF50; // 鲜绿色分割线
			height: 1px;
			width: 100%;
			position: absolute;
			top: 40px;
		}

		span {
			background: #4CAF50; // 鲜绿色标题背景
			line-height: 40px;
			position: absolute;
			width: 120px;
			left: 50%;
			margin-left: -60px;
			color: #fff;
		}
	}
}

.markdown-body {
	line-height: 1.6;
	font-size: 16px;
	color: #2E7D32; // 深绿色文字
}

.markdown-body pre {
	background: #E8F5E9; // 浅绿色背景
	padding: 10px;
	border-radius: 5px;
	overflow-x: auto;
}

.markdown-body code {
	background: #E8F5E9; // 浅绿色背景
	padding: 2px 5px;
	border-radius: 3px;
}

.system-predict-container {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	overflow: auto;

	.system-predict-padding {
		padding: 0 100px;
		overflow-y: auto;

		.el-table {
			flex: 1;
			--el-table-header-bg-color: #C8E6C9; // 绿色表头
			--el-table-row-hover-bg-color: #E8F5E9; // 浅绿色悬浮
			--el-table-border-color: #C8E6C9; // 绿色边框
		}
	}
}

.header {
	width: 100%;
	height: 5%;
	display: flex;
	justify-content: start;
	align-items: center;
	font-size: 20px;
}

.card {
	width: 100%;
	height: 100%;
	border-radius: 10px;
	margin-top: 15px;
	display: flex;
	justify-content: center;
	align-items: center;
	background: #F1F8E9; // 浅绿色背景
	border: 1px solid #C8E6C9; // 绿色边框
}

.avatar-uploader .avatar {
	width: 100%;
	height: 600px;
	display: block;
}

.el-icon.avatar-uploader-icon {
	font-size: 28px;
	color: #81C784; // 浅绿色图标
	width: 100%;
	height: 600px;
	text-align: center;
}

.button-section {
	display: flex;
	justify-content: center;
}

.predict-button {
	background: #4CAF50; // 鲜绿色按钮
	border-color: #4CAF50;
	width: 100%;

	&:hover {
		background: #388E3C; // 稍深的绿色
		border-color: #388E3C;
	}
}

.result-section {
	width: 50%;
	height: 100%;
	margin-top: 15px;
	margin-left: 15px;
	text-align: center;
	display: flex;
	flex-direction: column;
	border-radius: 6px;
	background: #F1F8E9; // 浅绿色背景
	border: 1px solid #C8E6C9; // 绿色边框
}

.bottom {
	width: 100%;
	font-size: 18px;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
}

.result-content {
	width: 100%;
	margin-top: 16px;

	.info-card {
		padding: 20px;
		border-radius: 8px;
		background: #E8F5E9; // 浅绿色背景
		box-shadow: 0 2px 12px 0 rgba(76, 175, 80, 0.1); // 绿色阴影
	}

	.info-item {
		margin: 12px 0;
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		padding: 8px 0;
		border-bottom: 1px solid #C8E6C9; // 绿色分割线

		&:last-child {
			border-bottom: none;
		}

		.info-label {
			display: flex;
			align-items: center;
			color: #2E7D32; // 深绿色文字
			font-size: 14px;

			.icon {
				margin-right: 8px;
				font-size: 16px;
				color: #4CAF50; // 鲜绿色图标
			}
		}

		.info-value {
			font-size: 16px;
			font-weight: 500;
			color: #2E7D32; // 深绿色文字
			flex: 1;
			margin-left: 16px;

			&.highlight {
				color: #4CAF50; // 鲜绿色高亮
				font-weight: 600;
			}

			&.accent {
				color: #81C784; // 浅绿色
			}

			&::-webkit-scrollbar {
				width: 6px;
			}

			&::-webkit-scrollbar-thumb {
				background: #C8E6C9; // 绿色滚动条
				border-radius: 4px;
			}

			&::-webkit-scrollbar-track {
				background: #E8F5E9; // 浅绿色轨道
				border-radius: 4px;
			}
		}
	}
}

:deep(.el-slider__bar) {
	background-color: #4CAF50; // 滑块进度条
}

:deep(.el-slider__button) {
	border-color: #4CAF50; // 滑块按钮
}
</style>