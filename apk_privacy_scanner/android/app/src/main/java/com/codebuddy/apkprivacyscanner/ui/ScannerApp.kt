package com.codebuddy.apkprivacyscanner.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codebuddy.apkprivacyscanner.file.readApkFileState
import com.codebuddy.apkprivacyscanner.model.ApkFileUiState
import com.codebuddy.apkprivacyscanner.model.ApkScanResult
import com.codebuddy.apkprivacyscanner.report.MarkdownReportGenerator
import com.codebuddy.apkprivacyscanner.scanner.ApkStaticScanner
import com.codebuddy.apkprivacyscanner.ui.components.HeaderSection
import com.codebuddy.apkprivacyscanner.ui.components.PrivacyNotice
import com.codebuddy.apkprivacyscanner.ui.components.StatusCards
import com.codebuddy.apkprivacyscanner.ui.screens.HomeContent
import com.codebuddy.apkprivacyscanner.ui.screens.ResultContent
import com.codebuddy.apkprivacyscanner.ui.screens.ScanningContent
import com.codebuddy.apkprivacyscanner.ui.theme.ApkPrivacyScannerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ScreenState {
    Home,
    Scanning,
    Result
}

@Composable
fun ScannerApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val coroutineScope = rememberCoroutineScope()
    var screenState by remember { mutableStateOf(ScreenState.Home) }
    var selectedApk by remember { mutableStateOf<ApkFileUiState?>(null) }
    var scanResult by remember { mutableStateOf<ApkScanResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            errorMessage = "未选择 APK 文件"
            return@rememberLauncherForActivityResult
        }

        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        selectedApk = readApkFileState(context, uri)
        scanResult = null
        errorMessage = if (selectedApk?.isApk == true) {
            null
        } else {
            "已选择文件，但文件名或 MIME 类型未识别为 APK，请确认文件是否正确。"
        }
        screenState = ScreenState.Home
    }

    fun scanSelectedApk() {
        val apk = selectedApk ?: return
        if (!apk.isApk) {
            errorMessage = "请先选择有效的 APK 文件"
            return
        }

        screenState = ScreenState.Scanning
        scanResult = null
        errorMessage = null

        coroutineScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    ApkStaticScanner(appContext).scan(apk.uri)
                }
            }

            result
                .onSuccess { summary ->
                    scanResult = summary
                    errorMessage = null
                    screenState = ScreenState.Result
                }
                .onFailure { error ->
                    errorMessage = error.message ?: "读取 APK 结构或 DEX 字符串失败"
                    scanResult = null
                    screenState = ScreenState.Home
                }
        }
    }

    fun shareMarkdownReport() {
        val apk = selectedApk ?: return
        val result = scanResult ?: return
        val report = MarkdownReportGenerator().generate(apk = apk, result = result)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "APK Privacy Scanner 扫描报告")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        context.startActivity(
            Intent.createChooser(sendIntent, "分享 Markdown 报告")
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderSection()
            StatusCards(
                selectedApk = selectedApk,
                scanResult = scanResult
            )

            when (screenState) {
                ScreenState.Home -> HomeContent(
                    selectedApk = selectedApk,
                    scanResult = scanResult,
                    errorMessage = errorMessage,
                    onSelectApk = {
                        apkPickerLauncher.launch(
                            arrayOf(
                                "application/vnd.android.package-archive",
                                "application/octet-stream",
                                "application/zip",
                                "application/x-zip-compressed",
                                "*/*"
                            )
                        )
                    },
                    onScanApk = ::scanSelectedApk,
                    onPreviewResult = { screenState = ScreenState.Result }
                )

                ScreenState.Scanning -> ScanningContent(
                    selectedApk = selectedApk,
                    onBack = { screenState = ScreenState.Home },
                    onShowResult = { screenState = ScreenState.Result }
                )

                ScreenState.Result -> ResultContent(
                    selectedApk = selectedApk,
                    scanResult = scanResult,
                    onShareReport = ::shareMarkdownReport,
                    onBack = { screenState = ScreenState.Home }
                )
            }

            PrivacyNotice()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScannerAppPreview() {
    ApkPrivacyScannerTheme {
        ScannerApp()
    }
}
