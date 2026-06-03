package com.codebuddy.apkprivacyscanner.scanner

import android.content.Context
import android.net.Uri
import com.codebuddy.apkprivacyscanner.model.ApkScanResult
import com.codebuddy.apkprivacyscanner.model.ApkStructureSummary
import com.codebuddy.apkprivacyscanner.model.RiskItem
import com.codebuddy.apkprivacyscanner.model.RiskItemType
import com.codebuddy.apkprivacyscanner.model.RiskLevel
import com.codebuddy.apkprivacyscanner.rules.NetworkRules
import java.util.zip.ZipInputStream

class ApkStaticScanner(private val context: Context) {
    fun scan(uri: Uri): ApkScanResult {
        val dexFiles = mutableListOf<String>()
        val metaInfFiles = mutableListOf<String>()
        val nativeLibraries = mutableListOf<String>()
        val assetEntries = mutableListOf<String>()
        val resourceEntries = mutableListOf<String>()
        val sampleEntries = mutableListOf<String>()
        var totalEntries = 0
        var hasManifest = false

        val dexStringExtractor = DexStringExtractor()
        val dexAccumulator = dexStringExtractor.createAccumulator()

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: error("无法打开用户选择的 APK 文件")

        inputStream.use { rawStream ->
            ZipInputStream(rawStream.buffered()).use { zipStream ->
                while (true) {
                    val entry = zipStream.nextEntry ?: break
                    val name = entry.name
                    totalEntries += 1

                    if (sampleEntries.size < MAX_SAMPLE_ENTRIES) {
                        sampleEntries += name
                    }

                    when {
                        name == "AndroidManifest.xml" -> hasManifest = true
                        name.matches(DEX_FILE_REGEX) -> {
                            dexFiles += name
                            dexStringExtractor.consumeDexEntry(
                                dexFileName = name,
                                inputStream = zipStream,
                                accumulator = dexAccumulator
                            )
                        }
                        name.startsWith("META-INF/") -> metaInfFiles += name
                        name.startsWith("lib/") && name.endsWith(".so") -> nativeLibraries += name
                        name.startsWith("assets/") -> assetEntries += name
                        name.startsWith("res/") -> resourceEntries += name
                    }

                    zipStream.closeEntry()
                }
            }
        }

        val structureSummary = ApkStructureSummary(
            totalEntries = totalEntries,
            hasManifest = hasManifest,
            dexFiles = dexFiles,
            metaInfFiles = metaInfFiles,
            nativeLibraries = nativeLibraries,
            assetEntries = assetEntries,
            resourceEntries = resourceEntries,
            sampleEntries = sampleEntries
        )
        val dexStringSummary = dexStringExtractor.buildSummary(dexAccumulator)
        val stringsForAnalysis = buildList {
            addAll(dexStringSummary.stringSamples)
            addAll(dexStringSummary.urlLikeSamples)
            addAll(dexStringSummary.apiLikeSamples)
            addAll(dexStringSummary.packageLikeSamples)
        }
        val trackerFindings = TrackerAnalyzer().analyze(stringsForAnalysis)
        val sensitiveApiFindings = SensitiveApiAnalyzer().analyze(stringsForAnalysis)
        val networkFinding = NetworkAnalyzer().analyze(stringsForAnalysis)
        val riskScoreResult = RiskScorer().score(
            matchedTrackers = trackerFindings.map { it.evidence },
            matchedSensitiveApis = sensitiveApiFindings.map { it.evidence },
            extraItems = networkFinding.toRiskItems()
        )

        return ApkScanResult(
            structureSummary = structureSummary,
            dexStringSummary = dexStringSummary,
            trackerFindings = trackerFindings,
            sensitiveApiFindings = sensitiveApiFindings,
            networkFinding = networkFinding,
            riskScoreResult = riskScoreResult
        )
    }

    private fun com.codebuddy.apkprivacyscanner.model.NetworkFinding.toRiskItems(): List<RiskItem> {
        val items = mutableListOf<RiskItem>()
        suspiciousDomains.forEach { domain ->
            items += RiskItem(
                type = RiskItemType.Network,
                title = "可疑域名：$domain",
                description = "域名命中可疑顶级域名列表",
                score = NetworkRules.suspiciousDomainScore,
                evidence = domain,
                riskLevel = RiskLevel.MEDIUM
            )
        }
        if (domainCount > LARGE_DOMAIN_THRESHOLD) {
            items += RiskItem(
                type = RiskItemType.Network,
                title = "大量硬编码域名",
                description = "检测到较多硬编码域名",
                score = NetworkRules.largeDomainCountScore,
                evidence = domainCount.toString(),
                riskLevel = RiskLevel.LOW
            )
        }
        return items
    }

    private companion object {
        const val MAX_SAMPLE_ENTRIES = 20
        const val LARGE_DOMAIN_THRESHOLD = 20
        val DEX_FILE_REGEX = Regex("classes(\\d*)\\.dex")
    }
}
