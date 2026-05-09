<template>
	<div class="home-container layout-pd">
		<el-row :gutter="15" class="home-card-two mb15">
			<el-col :xs="24" :sm="14" :md="14" :lg="16" :xl="16">
				<div class="home-card-item">
					<div style="height: 100%" ref="homeBarRef"></div>
				</div>
			</el-col>
			<el-col :xs="24" :sm="10" :md="10" :lg="8" :xl="8" class="home-media">
				<div class="home-card-item">
					<div style="height: 100%" ref="homePieRef"></div>
				</div>
			</el-col>
		</el-row>
		<el-row :gutter="15" class="home-card-three">
			<el-col :xs="24" :sm="14" :md="14" :lg="8" :xl="8" class="home-media">
				<div class="home-card-item">
					<div style="height: 100%" ref="homeradarRef"></div>
				</div>
			</el-col>
			<el-col :xs="24" :sm="10" :md="10" :lg="16" :xl="16">
				<div class="home-card-item">
					<div class="home-card-item-title">实时预测信息</div>
					<div class="home-monitor">
						<div class="flex-warp">
							<el-table :data="state.data.slice(0,6)" style="width: 100%">
								<el-table-column prop="username" label="用户名" align="center" />
								<el-table-column prop="weight" label="识别权重" align="center" />
								<el-table-column prop="conf" label="最小阈值" align="center" />
								<el-table-column prop="ai" label="AI助手" align="center" />
								<el-table-column prop="startTime" label="时间" align="center" />
							</el-table>
						</div>
					</div>
				</div>
			</el-col>
		</el-row>
		<el-row :gutter="15" class="home-card-three" style="margin-top: 15px;">
			<el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
				<div class="home-card-item">
					<div style="height: 100%" ref="homeLineRef"></div>
				</div>
			</el-col>
		</el-row>
	</div>
</template>

<script setup lang="ts" name="home">
import { reactive, onMounted, ref, watch, nextTick, onActivated, markRaw } from 'vue';
import * as echarts from 'echarts';
import { storeToRefs } from 'pinia';
import { useThemeConfig } from '/@/stores/themeConfig';
import { useTagsViewRoutes } from '/@/stores/tagsViewRoutes';
import request from '/@/utils/request';

// 定义变量内容
const homeLineRef = ref();
const homePieRef = ref();
const homeBarRef = ref();
const homeradarRef = ref();
const storesTagsViewRoutes = useTagsViewRoutes();
const storesThemeConfig = useThemeConfig();
const { themeConfig } = storeToRefs(storesThemeConfig);
const { isTagsViewCurrenFull } = storeToRefs(storesTagsViewRoutes);
const state = reactive({
	data: [] as any,
	global: {
		homeChartOne: null,
		homeChartTwo: null,
		homeCharThree: null,
		homeCharFour: null,
		dispose: [null, '', undefined],
	} as any,
	myCharts: [] as EmptyArrayType,
	charts: {
		theme: '',
		bgColor: '',
		color: '#2E7D32', // 深绿色标题文字
	},
});

// 折线图
const initLineChart = () => {
	if (!state.global.dispose.some((b: any) => b === state.global.homeChartOne)) state.global.homeChartOne.dispose();
	state.global.homeChartOne = markRaw(echarts.init(homeLineRef.value, state.charts.theme));
	const counts: Record<string, number> = state.data.reduce((acc, prediction) => {
		const date = prediction.startTime.split(' ')[0];
		acc[date] = (acc[date] || 0) + 1;
		return acc;
	}, {} as Record<string, number>);
	const sortedDatesDesc = Object.keys(counts).sort((a, b) => b.localeCompare(a));
	const latest7DatesDesc = sortedDatesDesc.slice(0, 10);
	const latest7Dates = latest7DatesDesc.sort((a, b) => a.localeCompare(b));
	const result = {
		dateData: latest7Dates,
		valueData: latest7Dates.map(date => counts[date])
	};
	const option = {
		// backgroundColor: state.charts.bgColor,
		title: {
			text: '近十日预测数量',
			x: 'left',
			textStyle: { fontSize: 15, color: state.charts.color },
		},
		grid: { top: 70, right: 20, bottom: 30, left: 30 },
		tooltip: { trigger: 'axis' },
		xAxis: {
			data: result.dateData,
			axisLabel: { color: '#1A3C34' }, // 深青绿
		},
		yAxis: [
			{
				type: 'value',
				name: '预测数量',
				splitLine: { show: true, lineStyle: { type: 'dashed', color: '#E8F5E9' } }, // 浅绿分割线
				axisLabel: { color: '#1A3C34' },
			},
		],
		series: [
			{
				name: '数量',
				type: 'line',
				symbolSize: 6,
				symbol: 'circle',
				smooth: true,
				data: result.valueData,
				lineStyle: { color: '#4CAF50' }, // 鲜绿色线条
				itemStyle: { color: '#4CAF50', borderColor: '#4CAF50' },
				areaStyle: {
					color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
						{ offset: 0, color: '#4CAF50b3' }, // 鲜绿色半透明
						{ offset: 1, color: '#4CAF5003' }, // 透明
					]),
				},
				emphasis: {
					itemStyle: {
						color: {
							type: 'radial',
							x: 0.5,
							y: 0.5,
							r: 0.5,
							colorStops: [
								{ offset: 0, color: '#4CAF50' },
								{ offset: 0.4, color: '#4CAF50' },
								{ offset: 0.5, color: '#fff' },
								{ offset: 0.7, color: '#fff' },
								{ offset: 0.8, color: '#fff' },
								{ offset: 1, color: '#fff' },
							],
						},
						borderColor: '#4CAF50',
						borderWidth: 2,
					},
				},
			},
		],
	};
	state.global.homeChartOne.setOption(option);
	state.myCharts.push(state.global.homeChartOne);
};

// 饼图
const initPieChart = () => {
	if (!state.global.dispose.some((b: any) => b === state.global.homeChartTwo)) state.global.homeChartTwo.dispose();
	state.global.homeChartTwo = markRaw(echarts.init(homePieRef.value, state.charts.theme));
	const usernameCounts = state.data.reduce((acc, prediction) => {
		const username = prediction.username;
		acc[username] = (acc[username] || 0) + 1;
		return acc;
	}, {});
	const sortedUsernames = Object.keys(usernameCounts).sort((a, b) => usernameCounts[b] - usernameCounts[a]);
	const top5Usernames = sortedUsernames.slice(0, 6);
	const top5Values = top5Usernames.map(u => usernameCounts[u]);
	const result = {
		usernameData: top5Usernames,
		valueData: top5Values
	};
	const pieData = result.usernameData.map((username, i) => ({
		name: username,
		value: result.valueData[i]
	}));
	const colorList = [
		{
			type: 'linear',
			x: 0,
			y: 0,
			x2: 1,
			y2: 1,
			colorStops: [
				{ offset: 0, color: 'rgba(76,175,80,0.01)' }, // 鲜绿
				{ offset: 1, color: 'rgba(76,175,80,0.57)' }
			],
			globalCoord: false
		},
		{
			type: 'linear',
			x: 1,
			y: 0,
			x2: 0,
			y2: 1,
			colorStops: [
				{ offset: 0, color: 'rgba(129,199,132,0.02)' }, // 浅绿
				{ offset: 1, color: 'rgba(129,199,132,0.67)' }
			],
			globalCoord: false
		},
		{
			type: 'linear',
			x: 1,
			y: 0,
			x2: 0,
			y2: 0,
			colorStops: [
				{ offset: 0, color: 'rgba(165,214,167,0.02)' }, // 薄荷绿
				{ offset: 1, color: 'rgba(165,214,167,0.57)' }
			],
			globalCoord: false
		},
		{
			type: 'linear',
			x: 0,
			y: 1,
			x2: 0,
			y2: 0,
			colorStops: [
				{ offset: 0, color: 'rgba(102,187,106,0.01)' }, // 中绿
				{ offset: 1, color: 'rgba(102,187,106,0.57)' }
			],
			globalCoord: false
		},
		{
			type: 'linear',
			x: 1,
			y: 1,
			x2: 1,
			y2: 0,
			colorStops: [
				{ offset: 0, color: 'rgba(56,142,60,0.01)' }, // 深绿
				{ offset: 1, color: 'rgba(56,142,60,0.57)' }
			],
			globalCoord: false
		},
		{
			type: 'linear',
			x: 0,
			y: 0,
			x2: 1,
			y2: 0,
			colorStops: [
				{ offset: 0, color: 'rgba(139,195,74,0.12)' }, // 草绿
				{ offset: 1, color: 'rgba(139,195,74,0.57)' }
			],
			globalCoord: false
		}
	];
	const colorLine = ['#4CAF50', '#81C784', '#A5D6A7', '#66BB6A', '#388E3C', '#8BC34A']; // 绿色系线条
	function getRich() {
		let rich = {};
		colorLine.forEach((v, i) => {
			rich[`hr${i}`] = {
				backgroundColor: colorLine[i],
				borderRadius: 3,
				width: 3,
				height: 3,
				padding: [3, 3, 0, -12]
			};
			rich[`a${i}`] = {
				padding: [-11, 6, -20, 6],
				color: colorLine[i],
				backgroundColor: 'transparent',
				fontSize: 12
			};
		});
		return rich;
	}
	pieData.forEach((v: any, i) => {
		v.labelLine = {
			lineStyle: {
				width: 1,
				color: colorLine[i]
			}
		};
	});
	const option = {
		backgroundColor: state.charts.bgColor,
		title: {
			text: '不同用户的预测个数',
			x: 'left',
			textStyle: { fontSize: '15', color: state.charts.color },
		},
		legend: { top: 'bottom' },
		tooltip: { trigger: 'item' },
		series: [
			{
				type: 'pie',
				radius: '60%',
				center: ['50%', '50%'],
				clockwise: true,
				avoidLabelOverlap: true,
				label: {
					show: true,
					position: 'outside',
					formatter: function (params) {
						const name = params.name;
						const percent = params.percent + '%';
						const index = params.dataIndex;
						return [`{a${index}|${name}：${percent}}`, `{hr${index}|}`].join('\n');
					},
					rich: getRich()
				},
				itemStyle: {
					normal: {
						color: function (params) {
							return colorList[params.dataIndex];
						}
					}
				},
				data: pieData,
				roseType: 'radius'
			}
		]
	};
	state.global.homeChartTwo.setOption(option);
	state.myCharts.push(state.global.homeChartTwo);
};

// 雷达图
const initradarChart = () => {
	if (!state.global.dispose.some((b: any) => b === state.global.homeCharFour)) state.global.homeCharFour.dispose();
	state.global.homeCharFour = markRaw(echarts.init(homeradarRef.value, state.charts.theme));
	const confStatsByUser = state.data.reduce((acc, prediction) => {
		const { username, confidence } = prediction;
		const confidences = JSON.parse(confidence).map(percentStr => {
			const numValue = parseFloat(percentStr.replace('%', '')) / 100;
			return Number(numValue.toFixed(4));
		});
		const predictionAvg = confidences.reduce((sum, val) => sum + val, 0) / confidences.length;
		if (!acc[username]) {
			acc[username] = { total: predictionAvg, count: 1 };
		} else {
			acc[username].total += predictionAvg;
			acc[username].count += 1;
		}
		return acc;
	}, {});
	const avgConfData = Object.keys(confStatsByUser).map(username => ({
		username,
		avgConf: confStatsByUser[username].total / confStatsByUser[username].count,
	}));
	const top7AvgConfData = avgConfData.slice(0, 7);
	const result = {
		usernameData: top7AvgConfData.map(item => item.username),
		valueData: top7AvgConfData.map(item => Number((item.avgConf * 100).toFixed(2))),
	};
	const data = top7AvgConfData.map(item => Number((item.avgConf * 100).toFixed(2)));
	const indicatorNames = top7AvgConfData.map(item => item.username);
	const maxData = Array(data.length).fill(100);
	const indicator = indicatorNames.map((name, idx) => ({ name, max: maxData[idx] }));
	const innerData = i => Array(data.length).fill(100 - 20 * i);
	const getData = data => {
		const series = [
			{
				type: 'radar',
				symbolSize: 10,
				symbol: 'circle',
				areaStyle: {
					color: 'rgba(76,175,80,0.5)', // 鲜绿色区域
					opacity: 0.3,
				},
				lineStyle: {
					color: new echarts.graphic.LinearGradient(
						0, 0, 0, 1,
						[
							{ offset: 0, color: 'rgba(76,175,80,0)' },
							{ offset: 1, color: 'rgba(76,175,80,0.3)' },
						],
						false
					),
					width: 3,
				},
				itemStyle: {
					color: '#fff',
					borderColor: new echarts.graphic.LinearGradient(
						0, 0, 0, 1,
						[
							{ offset: 0, color: 'rgba(76,175,80,0)' },
							{ offset: 1, color: 'rgba(76,175,80,0.3)' },
						],
						false
					),
					borderWidth: 4,
					opacity: 1,
				},
				label: { show: false },
				data: [{ value: data }],
				z: 100,
			},
		] as any;
		data.forEach((_, i) => {
			series.push({
				type: 'radar',
				data: [{ value: innerData(i) }],
				symbol: 'none',
				lineStyle: { width: 0 },
				itemStyle: { color: '#fff' },
				areaStyle: {
					color: '#fff',
					shadowColor: 'rgba(76,175,80,0.15)', // 鲜绿色阴影
					shadowBlur: 30,
					shadowOffsetY: 20,
				},
			});
		});
		return { series };
	};
	const optionData = getData(data);
	const option = {
		backgroundColor: state.charts.bgColor,
		title: {
			text: '不同用户间的平均置信度',
			x: 'left',
			textStyle: { fontSize: '15', color: state.charts.color },
		},
		tooltip: {
			formatter: () =>
				indicatorNames
					.map((name, i) => `${name} : ${data[i]}%`)
					.join('<br>'),
		},
		radar: {
			radius: '70%',
			center: ['50%', '50%'],
			indicator: indicator,
			splitArea: {
				show: true,
				areaStyle: {
					color: '#fff',
					shadowColor: 'rgba(76,175,80,0.3)', // 鲜绿色阴影
					shadowBlur: 30,
					shadowOffsetY: 20,
				},
			},
			splitLine: { show: false },
			axisLine: { show: false },
			axisLabel: { show: false },
			name: {
				textStyle: {
					rich: {
						a: {
							fontSize: '14',
							color: '#2E7D32', // 深绿色
							align: 'left',
							lineHeight: '20',
							fontWeight: 'bold',
						},
						b: {
							fontSize: '12',
							color: '#4CAF50', // 鲜绿色
							align: 'left',
						},
					},
				},
				formatter: params => {
					const idx = indicatorNames.indexOf(params);
					const percent = data[idx];
					return `{a|${percent}%}\n{b|${params}}`;
				},
			},
		},
		series: optionData.series,
	};
	state.global.homeCharFour.setOption(option);
	state.myCharts.push(state.global.homeCharFour);
};

// 柱状图
const initBarChart = () => {
	if (!state.global.dispose.some((b: any) => b === state.global.homeCharThree)) 
		state.global.homeCharThree.dispose();
	state.global.homeCharThree = markRaw(echarts.init(homeBarRef.value, state.charts.theme));
	const categories = [
		"苹果-黑腐病", "苹果-健康", "苹果-结痂", "甜椒-细菌性斑疹", "甜椒-健康",
		"木薯-褐条病", "木薯-细菌性枯萎病", "木薯-绿斑病", "樱桃-健康", "樱桃-白粉病",
		"玉米-叶斑病", "玉米-普通锈病", "玉米-健康", "葡萄-黑腐病", "葡萄-健康",
		"葡萄-叶枯病", "木薯-健康", "木薯-花叶病毒", "玉米-大斑病", "柑桔-黄龙病",
		"桃子-桃细菌性穿孔病", "桃子-健康", "土豆-早疫病", "土豆-健康", "土豆-晚疫病",
		"水稻-褐斑病", "水稻-健康", "水稻-稻瘟病", "草莓-白粉病", "草莓-健康",
		"草莓-角斑病", "番茄-细菌性斑疹病", "番茄-早疫病", "番茄-晚疫病", "番茄-健康",
		"番茄-叶霉病", "番茄-斑枯病"
	];
	function countCategories(data: any[]): [string[], string[]] {
		const counts: Record<string, number> = {};
		categories.forEach(cat => {
			counts[cat] = 0;
		});
		data.forEach(item => {
			try {
				const labels: string[] = JSON.parse(item.label);
				labels.forEach(label => {
					const baseCategory = categories.find(cat => label.includes(cat));
					if (baseCategory) {
						counts[baseCategory]++;
					}
				});
			} catch (error) {
				console.error('解析 label 失败：', item, error);
			}
		});
		const countStrings = categories.map(cat => counts[cat].toString());
		return [categories, countStrings];
	}
	const [catArr, countsArr] = countCategories(state.data);
	const option = {
		backgroundColor: state.charts.bgColor,
		title: {
			text: '不同种类的检测个数',
			x: 'left',
			textStyle: { fontSize: '15', color: state.charts.color },
		},
		tooltip: { trigger: 'axis' },
		grid: { top: 70, right: 80, bottom: 30, left: 80 },
		xAxis: [
			{
				type: 'category',
				data: catArr,
				boundaryGap: true,
				axisTick: { show: false },
				axisLabel: { color: '#1A3C34' }, // 深青绿
			},
		],
		yAxis: [
			{
				type: 'value',
				name: '个数',
				splitLine: { show: true, lineStyle: { type: 'dashed', color: '#E8F5E9' } }, // 浅绿分割线
				axisLabel: { color: '#1A3C34' },
			},
		],
		series: [
			{
				name: '检测个数',
				type: 'bar',
				barWidth: 10,
				itemStyle: {
					color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
						{ offset: 0, color: 'rgba(76,175,80,0.3)' }, // 鲜绿色
						{ offset: 1, color: 'rgba(76,175,80,0)' },
					]),
					borderRadius: [30, 30, 0, 0],
				},
				data: countsArr,
			},
		],
	};
	state.global.homeCharThree.setOption(option);
	state.myCharts.push(state.global.homeCharThree);
};

// 批量设置 echarts resize
const initEchartsResizeFun = () => {
	nextTick(() => {
		for (let i = 0; i < state.myCharts.length; i++) {
			setTimeout(() => {
				state.myCharts[i].resize();
			}, i * 1000);
		}
	});
};
const initEchartsResize = () => {
	window.addEventListener('resize', initEchartsResizeFun);
};
onMounted(() => {
	request.get('/api/imgRecords/all').then((res) => {
		if (res.code == 0) {
			state.data = res.data.reverse();
		}
	});
	initEchartsResize();
});
onActivated(() => {
	initEchartsResizeFun();
});
watch(
	() => isTagsViewCurrenFull.value,
	() => {
		initEchartsResizeFun();
	}
);
watch(
	() => themeConfig.value.isIsDark,
	(isIsDark) => {
		nextTick(() => {
			state.charts.theme = isIsDark ? 'dark' : '';
			state.charts.bgColor = isIsDark ? 'transparent' : '';
			state.charts.color = isIsDark ? '#C8E6C9' : '#2E7D32'; // 深绿色（亮绿在暗模式下）
			setTimeout(() => {
				initLineChart();
				initradarChart();
				initPieChart();
				initBarChart();
			}, 500);
		});
	},
	{ deep: true, immediate: true }
);
</script>

<style scoped lang="scss">
$homeNavLengh: 8;

.home-container {
	overflow: hidden;

	.home-card-one,
	.home-card-two,
	.home-card-three {
		.home-card-item {
			width: 100%;
			height: 130px;
			border-radius: 4px;
			transition: all ease 0.3s;
			padding: 20px;
			overflow: hidden;
			color: #2E7D32; // 深绿色文字
			border: 1px solid #C8E6C9; // 绿色边框

			&:hover {
				box-shadow: 0 2px 12px rgba(76, 175, 80, 0.3); // 绿色阴影
				transition: all ease 0.3s;
			}

			&-icon {
				width: 70px;
				height: 70px;
				border-radius: 100%;
				flex-shrink: 1;

				i {
					color: #81C784; // 浅绿色图标
				}
			}

			&-title {
				font-size: 15px;
				font-weight: bold;
				height: 30px;
				color: #2E7D32; // 深绿色标题
			}
		}
	}

	.home-card-two,
	.home-card-three {
		.home-card-item {
			height: 400px;
			width: 100%;
			overflow: hidden;

			.home-monitor {
				height: 100%;

				.flex-warp-item {
					width: 25%;
					height: 111px;
					display: flex;

					.flex-warp-item-box {
						margin: auto;
						text-align: center;
						color: #2E7D32; // 深绿色文字
						display: flex;
						border-radius: 5px;
						background: #E8F5E9; // 浅绿色背景
						cursor: pointer;
						transition: all 0.3s ease;

						&:hover {
							background: #C8E6C9; // 稍深的绿色
							transition: all 0.3s ease;
						}
					}

					@for $i from 0 through $homeNavLengh {
						.home-animation#{$i} {
							opacity: 0;
							animation-name: error-num;
							animation-duration: 0.5s;
							animation-fill-mode: forwards;
							animation-delay: calc($i/10) + s;
						}
					}
				}
			}
		}
	}

	:deep(.el-table) {
		--el-table-header-bg-color: #C8E6C9; // 绿色表头
		--el-table-row-hover-bg-color: #E8F5E9; // 浅绿色悬浮
		--el-table-border-color: #C8E6C9; // 绿色边框
	}
}
</style>