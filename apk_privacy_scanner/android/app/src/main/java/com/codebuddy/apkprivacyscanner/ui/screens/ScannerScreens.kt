package com.codebuddy.apkprivacyscanner.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codebuddy.apkprivacyscanner.model.ApkFileUiState
import com.codebuddy.apkprivacyscanner.model.ApkScanResult
import com.codebuddy.apkprivacyscanner.ui.components.AnalysisSummaryCard
import com.codebuddy.apkprivacyscanner.ui.components.DexStringSummaryCard
import com.codebuddy.apkprivacyscanner.ui.components.EntrySampleCard
import com.codebuddy.apkprivacyscanner.ui.components.ResultExplanationCard
import com.codebuddy.apkprivacyscanner.ui.components.RiskItemsGroupedCard
import com.codebuddy.apkprivacyscanner.ui.components.RiskScoreCard
import com.codebuddy.apkprivacyscanner.ui.components.SampleListCard
import com.codebuddy.apkprivacyscanner.ui.components.SelectedApkCard
import com.codebuddy.apkprivacyscanner.ui.components.StepTitle
import com.codebuddy.apkprivacyscanner.ui.components.StructureSummaryCard

@Composable
fun HomeContent(
    selectedApk: ApkFileUiState?,
    scanResult: ApkScanResult?,
    errorMessage: String?,
    onSelectApk: () -> Unit,
    onScanApk: () -> Unit,
    onPreviewResult: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "选择 APK 后开始分析",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "本阶段会串联 APK 结构读取、DEX 字符串提取、规则匹配和初版风险评分。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSelectApk
            ) {
                Text("选择 APK 文件")
            }

            selectedApk?.let { apk ->
                SelectedApkCard(apk = apk)
            }

            scanResult?.let { result ->
                RiskScoreCard(result = result.riskScoreResult)
                AnalysisSummaryCard(scanResult = result)
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedApk?.isApk == true,
                onClick = onScanApk
            ) {
                Text("开始基础扫描")
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = scanResult != null,
                onClick = onPreviewResult
            ) {
                Text("查看扫描结果")
            }
        }
    }
}

@Composable
fun ScanningContent(
    selectedApk: ApkFileUiState?,
    onBack: () -> Unit,
    onShowResult: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepTitle(
                index = "01",
                title = "基础扫描引擎",
                subtitle = "正在串联 APK 读取、DEX 字符串提取、规则匹配和风险评分。"
            )
            selectedApk?.let { apk ->
                Text(
                    text = "待扫描文件：${apk.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { 0.92f },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "当前阶段暂不解析 Manifest 权限，风险分数来自 Tracker、敏感 API 和网络线索。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onBack
                ) {
                    Text("返回首页")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onShowResult
                ) {
                    Text("查看结果")
                }
            }
        }
    }
}

@Composable
fun ResultContent(
    selectedApk: ApkFileUiState?,
    scanResult: ApkScanResult?,
    onShareReport: () -> Unit,
    onBack: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepTitle(
                index = "02",
                title = "扫描结果",
                subtitle = "展示风险总览、命中摘要和静态线索。可分享 Markdown 报告文本。"
            )
            selectedApk?.let { apk -> SelectedApkCard(apk = apk) }
            scanResult?.let { result ->
                RiskScoreCard(result = result.riskScoreResult)
                ResultExplanationCard()
                RiskItemsGroupedCard(items = result.riskScoreResult.items)
                AnalysisSummaryCard(scanResult = result)
                StructureSummaryCard(summary = result.structureSummary)
                DexStringSummaryCard(summary = result.dexStringSummary)
                SampleListCard(title = "Tracker / SDK 命中", items = result.trackerFindings.map { it.displayName })
                SampleListCard(title = "敏感 API 命中", items = result.sensitiveApiFindings.map { it.displayName })
                SampleListCard(title = "URL 样例", items = result.networkFinding.urls)
                SampleListCard(title = "IP 样例", items = result.networkFinding.ips)
                SampleListCard(title = "域名样例", items = result.networkFinding.domains)
                SampleListCard(title = "可疑域名", items = result.networkFinding.suspiciousDomains)
                EntrySampleCard(entries = result.structureSummary.sampleEntries)
                SampleListCard(title = "DEX 字符串样例", items = result.dexStringSummary.stringSamples)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onShareReport
                ) {
                    Text("分享 Markdown 报告")
                }
            } ?: Text(
                text = "尚未完成基础扫描，请先返回首页选择 APK 并点击扫描。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack
            ) {
                Text("返回首页")
            }
        }
    }
}
