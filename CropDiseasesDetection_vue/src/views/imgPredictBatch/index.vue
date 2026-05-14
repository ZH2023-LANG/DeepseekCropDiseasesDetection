<template>
    <div class="system-predict-container layout-padding" id="id" v-loading="state.loading">
        <div class="system-predict-padding layout-padding-auto layout-padding-view">
            <div class="carousel">
                <div class="section-title"><i></i><span>批量检测</span></div>
            </div>
            <div class="header">
                <div class="weight">
                    <el-select v-model="weight" placeholder="请选择模型" size="large" style="width: 200px">
                        <el-option v-for="item in state.weight_items" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                </div>
                <div class="button-section">
                    <el-button type="primary" @click="triggerImgFolderUpload" class="predict-button">上传图片文件夹</el-button>
                    <input type="file" ref="imgFolder" style="display: none;" webkitdirectory directory multiple
                        accept="image/*" @change="handleImgFolderChange" />
                </div>
                <div class="conf" style="margin-left: 0px;display: flex; flex-direction: row;">
                    <div
                        style="font-size: 14px;margin-right: 20px;display: flex;justify-content: start;align-items: center;color: #909399;">
                        设置最小置信度阈值</div>
                    <el-slider v-model="conf" :format-tooltip="formatTooltip" style="width: 300px;" :min="0" :max="100" />
                </div>
                <div class="button-section" style="margin-left: 20px">
                    <el-button type="primary" @click="startBatchPredict" class="predict-button"
                        :disabled="!state.form.imgFolder.length">
                        开始预测
                    </el-button>
                </div>
            </div>
            <div style="width: 100%; height: 800px; display: flex; flex-direction: row; margin-bottom: 20px;">
                <!-- 预测前显示上传提示 -->
                <div v-if="!state.data.length" style="width: 100%; display: flex; flex-direction: column;">
                    <el-card shadow="hover" class="card" style="text-align: center;">
                        <p>请上传待检测图片文件夹，然后点击“开始预测”</p>
                    </el-card>
                </div>
                <!-- 预测后显示表格 -->
                <div v-if="state.data.length"
                    style="width: 70%; display: flex; flex-direction: column; margin-right: 15px;">
                    <el-card shadow="hover" class="card">
                        <el-table :data="state.data" style="width: 100%" height="100%">
                            <el-table-column prop="index" align="center" label="序号" />
                            <el-table-column prop="label[0]" align="center" label="识别结果" />
                            <el-table-column prop="confidence[0]" align="center" label="预测概率" />
                            <el-table-column prop="allTime" align="center" label="总耗时" />
                            <el-table-column prop="startTime" align="center" label="预测时间" />
                            <el-table-column prop="outImg" label="预测图片" width="120" align="center">
                                <template #default="scope">
                                    <img :src="scope.row.outImg" width="120" height="60" />
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" align="center">
                                <template #default="{ $index }">
                                    <el-button type="primary" size="small" @click="selectResult($index)"
                                        :disabled="$index == state.selectedIndex">
                                        {{ $index == state.selectedIndex ? '已选择' : '选择' }}
                                    </el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-card>
                </div>
                <!-- 预测后显示选中结果 -->
                <el-card class="result-section" v-if="state.data.length">
                    <div class="bottom">
                        <div class="result-content">
                            <el-card shadow="hover" class="image-card">
                                <img v-if="outputImageUrl" :src="outputImageUrl" style="width: 100%;" />
                            </el-card>
                            <el-card shadow="never" class="info-card">
                                <div class="info-item">
                                    <div class="info-label">
                                        <el-icon class="icon"><Select /></el-icon>
                                        <span>识别结果</span>
                                    </div>
                                    <div class="info-value highlight">{{ state.predictionResult.label || '-' }}</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">
                                        <el-icon class="icon"><Opportunity /></el-icon>
                                        <span>预测概率</span>
                                    </div>
                                    <div class="info-value accent">
                                        {{ state.predictionResult.confidence || '-' }}
                                    </div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">
                                        <el-icon class="icon"><Clock /></el-icon>
                                        <span>总耗时</span>
                                    </div>
                                    <div class="info-value">{{ state.predictionResult.allTime || '-' }}</div>
                                </div>
                            </el-card>
                        </div>
                        <div style="width: 100%; margin-top: 20px;">
                            <h4>详细结果</h4>
                            <el-table :data="detailedTableData" style="width: 100%">
                                <el-table-column prop="label" label="预测结果" align="center" />
                                <el-table-column prop="confidence" label="置信度" align="center" />
                                <el-table-column prop="allTime" label="总用时" align="center" />
                            </el-table>
                        </div>
                    </div>
                </el-card>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts" name="personal">
import { reactive, ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import request from '/@/utils/request';
import { Loading, Plus, Select, Opportunity, Clock } from '@element-plus/icons-vue';
import { useUserInfo } from '/@/stores/userInfo';
import { storeToRefs } from 'pinia';
import { SocketService } from '/@/utils/socket';
import JSZip from 'jszip';

const outputImageUrl = ref('');
const imgFolder = ref<HTMLInputElement | null>(null);
const conf = ref(50);
const weight = ref('');
const formatTooltip = (val: number) => {
    return `${val / 100}`;
};
const stores = useUserInfo();
const { userInfos } = storeToRefs(stores);
const state = reactive({
    loading: false,
    weight_items: [] as any,
    data: [] as any[],
    selectedIndex: 0,
    predictionResult: {
        label: '',
        confidence: '',
        allTime: '',
        suggestion: '' as any,
        startTime: '',
        allConfidences: {} as any,
    },
    form: {
        username: '',
        imgFolder: [] as File[],
        imgFolderUrl: '',
    },
});

const detailedTableData = computed(() => {
    if (state.data.length > 0 && state.selectedIndex >= 0 && state.selectedIndex < state.data.length) {
        const selectedItem = state.data[state.selectedIndex];
        // 确保 label 和 confidence 是数组且长度一致
        if (Array.isArray(selectedItem.label) && Array.isArray(selectedItem.confidence)) {
            return selectedItem.label.map((label: string, idx: number) => ({
                label: label,
                confidence: selectedItem.confidence[idx] || '-',
                allTime: selectedItem.allTime,
            }));
        }
    }
    return [];
});

// 上传文件夹触发
const triggerImgFolderUpload = () => {
    if (imgFolder.value) {
        imgFolder.value.click();
    }
};

// 处理文件夹选择
const handleImgFolderChange = async (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files) {
        state.form.imgFolder = Array.from(target.files).filter(file => file.type.startsWith('image/'));
        state.form.imgFolderUrl = await uploadFolderToBackend(state.form.imgFolder, 'img');
        ElMessage.success(`已上传待检测文件夹，包含 ${state.form.imgFolder.length} 张图片`);
    }
};

// 上传文件夹到后端
const uploadFolderToBackend = async (files: File[], type: 'img') => {
    const uploadUrl = '/api/files/uploadFolder';
    const zip = new JSZip();
    const folderName = `${type}${Date.now()}`;

    for (const file of files) {
        zip.file(file.name, file);
    }

    try {
        const zipBlob = await zip.generateAsync({ type: 'blob' });
        const formData = new FormData();
        formData.append('folder', zipBlob, `${folderName}.zip`);

        const response = await fetch(uploadUrl, {
            method: 'POST',
            body: formData,
        });
        const result = await response.json();

        if (result.code == 0) {
            return result.data;
        } else {
            throw new Error(`上传失败: ${result.msg}`);
        }
    } catch (error) {
        ElMessage.error(`上传图片文件夹失败`);
        return '';
    }
};

// 选择结果
const selectResult = (index: number) => {
    state.selectedIndex = index;
    const selected = state.data[index];
    state.predictionResult.label = selected.label;
    state.predictionResult.confidence = selected.confidence;
    state.predictionResult.allTime = selected.allTime;
    state.predictionResult.allConfidences = selected.confidence;
    state.predictionResult.suggestion = selected.Suggestion;
    state.predictionResult.startTime = selected.startTime;
    outputImageUrl.value = selected.outImg;
};

// 开始批量预测
const startBatchPredict = async () => {
    if (!state.form.imgFolder.length) {
        ElMessage.error('请先上传图片文件夹');
        return;
    }

    state.loading = true;
    state.form.username = userInfos.value.userName;

    try {
        const data = {
            username: state.form.username,
            imgFolderUrl: state.form.imgFolderUrl,
            conf: parseFloat(conf.value.toString()) / 100,
            weight: weight.value,
        };

        const res = await request.post('/api/flask/predictImgBatch', data);

        if (res.code == 0) {
            state.data = res.data.map((item: any, index: number) => ({ ...item, index: index + 1 }));
            for(var i=0;i<state.data.length;i++){
                state.data[i].confidence = JSON.parse(state.data[i].confidence);
                state.data[i].label = JSON.parse(state.data[i].label);
            }
            console.log(state.data);

            if (state.data.length > 0) {
                // 默认选中第一个结果
                state.selectedIndex = 0;
                const firstResult = state.data[0];
                state.predictionResult.label = firstResult.label;
                state.predictionResult.confidence = firstResult.confidence;
                state.predictionResult.allTime = firstResult.allTime;
                state.predictionResult.allConfidences = firstResult.confidence;
                state.predictionResult.suggestion = firstResult.Suggestion;
                state.predictionResult.startTime = firstResult.startTime;
                outputImageUrl.value = firstResult.outImg;
                ElMessage.success('批量预测完成！');
            } else {
                ElMessage.warning('预测结果为空！');
            }
        } else {
            ElMessage.error(res.message || '预测失败');
        }
    } catch (error) {
        console.error('批量预测出错:', error);
        ElMessage.error('预测失败，请检查网络或文件格式');
    } finally {
        state.loading = false;
    }
};

const socketService = new SocketService();

socketService.on('message', (data) => {
    console.log('Received message:', data);
    ElMessage({
        message: data,
        type: 'success',
        duration: 3000,
    });
});

onMounted(() => {
    request.get('/api/flask/file_names').then((res) => {
        if (res.code == 0) {
            res.data = JSON.parse(res.data);
            state.weight_items = res.data.weight_items;
        } else {
            ElMessage.error(res.msg);
        }
    });
});
</script>

<style scoped lang="scss">
.carousel {
    width: 100%;
    margin-bottom: 20px;

    .section-title {
        font-size: 20px;
        text-align: center;
        position: relative;
        padding: 20px 0;
        display: flex;
        justify-content: center;
        align-items: center;

        i {
            background: #4CAF50;
            height: 1px;
            width: 100%;
            position: absolute;
            top: 50%;
            z-index: 1;
        }

        span {
            background: #4CAF50;
            line-height: 40px;
            width: 120px;
            color: #fff;
            z-index: 2;
        }
    }
}

.system-predict-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    padding: 20px;

    .system-predict-padding {
        padding: 20px;
        background: #fff;
        border-radius: 8px;
        overflow-y: auto;
        flex: 1;
    }
}

.header {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;
    flex-wrap: wrap;
}

.card {
    width: 100%;
    height: 100%;
    border-radius: 10px;
    display: flex;
    flex-direction: column;
    overflow-y: scroll;

    /* Ensure table fits within card */
    .el-table {
        flex: 1;
        overflow-y: auto;
    }
}

.predict-button {
    background: #4CAF50;
    border-color: #4CAF50;
    &:hover {
        background: #2E7D32;
        border-color: #2E7D32;
    }
}

.result-section {
    width: 30%;
    height: 100%;
    border-radius: 10px;
    overflow: scroll;
    display: flex;
    flex-direction: column;

    .bottom {
        flex: 1;
        display: flex;
        flex-direction: column;
        padding: 20px;
    }

    .result-content {
        display: flex;
        flex-direction: column;
        gap: 20px;
        margin-bottom: 20px;
    }
}

.image-card {
    width: 100%;
    border-radius: 8px;
    overflow: hidden;

    img {
        width: 100%;
        height: auto;
        max-height: 200px;
        object-fit: contain;
        display: block;
    }
}

.info-card {
    padding: 20px;
    border-radius: 8px;
    background: #f8f9fa;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #eee;

    &:last-child {
        border-bottom: none;
    }

    .info-label {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #606266;
        font-size: 14px;

        .icon {
            font-size: 16px;
            color: #409eff;
        }
    }

    .info-value {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
        text-align: right;

        &.highlight {
            color: #67c23a;
        }

        &.accent {
            color: #e6a23c;
        }
    }
}

/* Responsive design */
@media (max-width: 1200px) {
    .system-predict-container {
        padding: 10px;
    }

    .system-predict-padding {
        padding: 10px;
    }

    .result-section {
        width: 100%;
        margin-top: 20px;
        margin-left: 0;
    }

    .card {
        width: 100%;
        margin-right: 0;
    }

    .header {
        flex-direction: column;
        align-items: flex-start;
    }
}

/* Ensure table images are properly sized */
.el-table {
    img {
        width: 120px;
        height: 60px;
        object-fit: cover;
        border-radius: 4px;
    }
}
</style>
