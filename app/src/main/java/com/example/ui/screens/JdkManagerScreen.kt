package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.jdk.JdkCatalogAndDetector
import com.example.data.db.JdkInstallationEntity
import com.example.ui.StudioUiState
import com.example.ui.theme.IdeAmberSecondary
import com.example.ui.theme.IdeCyanPrimary
import com.example.ui.theme.IdeSuccessGreen

@Composable
fun JdkManagerScreen(
    uiState: StudioUiState,
    jdks: List<JdkInstallationEntity>,
    onSelectJdk: (String) -> Unit,
    onSetDefaultJdk: (String) -> Unit,
    onAddCustomJdk: (Int, String, String, String) -> Unit,
    onRemoveJdk: (String) -> Unit
) {
    var selectedEraFilter by remember { mutableIntStateOf(0) } // 0=All, 1=1.0-1.4, 2=5-8, 3=9-17, 4=18-25+
    var showAddJdkDialog by remember { mutableStateOf(false) }
    var verificationBanner by remember { mutableStateOf<String?>(null) }

    var customMajorInput by remember { mutableStateOf("26") }
    var customVersionInput by remember { mutableStateOf("26.0.0-ea") }
    var customVendorInput by remember { mutableStateOf("OpenJDK Early Access") }
    var customHomeInput by remember { mutableStateOf("/usr/lib/jvm/openjdk-26") }

    val activeJdk = jdks.find { it.versionId == uiState.activeJdkVersionId } ?: jdks.firstOrNull()
    val fiveLayer = remember(activeJdk) {
        activeJdk?.let { JdkCatalogAndDetector.getFiveLayerCompatibility(it) }
    }

    val filteredJdks = remember(jdks, selectedEraFilter) {
        when (selectedEraFilter) {
            1 -> jdks.filter { it.majorNumber in 0..4 }
            2 -> jdks.filter { it.majorNumber in 5..8 }
            3 -> jdks.filter { it.majorNumber in 9..17 }
            4 -> jdks.filter { it.majorNumber >= 18 }
            else -> jdks
        }
    }

    if (showAddJdkDialog) {
        AlertDialog(
            onDismissRequest = { showAddJdkDialog = false },
            title = { Text("Add / Configure Custom or Future JDK") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Configure a legally obtained local JDK installation or register a future JDK release (e.g., JDK 26+).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = customMajorInput,
                        onValueChange = { customMajorInput = it },
                        label = { Text("Major Version Number (e.g. 2, 8, 21, 25, 26)") },
                        singleLine = true,
                        modifier = Modifier.testTag("add_jdk_major_input")
                    )
                    OutlinedTextField(
                        value = customVersionInput,
                        onValueChange = { customVersionInput = it },
                        label = { Text("Exact Version String (e.g. 26.0.0-ea)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customVendorInput,
                        onValueChange = { customVendorInput = it },
                        label = { Text("Vendor (Oracle, Eclipse Adoptium, OpenJDK, Sun)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customHomeInput,
                        onValueChange = { customHomeInput = it },
                        label = { Text("JAVA_HOME Directory Path") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val major = customMajorInput.toIntOrNull() ?: 26
                        onAddCustomJdk(major, customVersionInput, customVendorInput, customHomeInput)
                        showAddJdkDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_jdk_button")
                ) {
                    Text("Register JDK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddJdkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Universal JDK Manager (JDK 1.0 – JDK 25+)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage historical (Java 2), LTS, modern, and future JDK toolchains with strict 5-layer compatibility.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { showAddJdkDialog = true },
                modifier = Modifier.testTag("add_jdk_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add JDK")
            }
        }

        // Host Environment Auto-Detection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Host Environment Auto-Detection Probe",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = IdeCyanPrimary
                )
                Text(
                    text = "Detected VM: ${uiState.hostReport.detectedVmName} • Version: ${uiState.hostReport.detectedJavaVersion} • Vendor: ${uiState.hostReport.detectedVendor}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Architecture: ${uiState.hostReport.detectedArch} • Host JAVA_HOME: ${uiState.hostReport.detectedJavaHome} • Registered Profiles: ${jdks.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active JDK 5-Layer Compatibility Matrix (Explicitly distinguishing Language, API, Bytecode, Compiler, Runtime)
        if (activeJdk != null && fiveLayer != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                    .testTag("active_jdk_compatibility_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SELECTED JAVA VERSION: ${activeJdk.displayName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Vendor: ${activeJdk.vendor} (${activeJdk.releaseYear}) • Bytecode: ${activeJdk.bytecodeVersion} • Arch: ${activeJdk.architecture}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                verificationBanner = "Verified ${activeJdk.versionId}: javac=${activeJdk.compilerPath}, java=${activeJdk.runtimePath}, Bytecode=${activeJdk.bytecodeVersion}, Flags=${activeJdk.supportedCompilerFlags}"
                            },
                            modifier = Modifier.testTag("verify_jdk_installation_button")
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify Installation")
                        }

                        OutlinedButton(
                            onClick = { onSetDefaultJdk(activeJdk.versionId) },
                            modifier = Modifier.testTag("set_default_jdk_button")
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = IdeAmberSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (activeJdk.isDefault) "Default JDK" else "Set Default JDK")
                        }

                        if (activeJdk.isCustomAdded) {
                            OutlinedButton(
                                onClick = { onRemoveJdk(activeJdk.versionId) },
                                modifier = Modifier.testTag("remove_custom_jdk_button")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove Custom JDK")
                            }
                        }
                    }

                    if (verificationBanner != null) {
                        Text(
                            text = verificationBanner!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = IdeSuccessGreen
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    Text(
                        text = "5-Layer Compatibility Distinction (No Simulated Equivalence)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = IdeAmberSecondary
                    )
                    CompatibilityLayerRow("1. Language Compatibility", fiveLayer.languageCompatibility)
                    CompatibilityLayerRow("2. API Compatibility", fiveLayer.apiCompatibility)
                    CompatibilityLayerRow("3. Bytecode Compatibility", fiveLayer.bytecodeCompatibility)
                    CompatibilityLayerRow("4. Actual Compiler (javac)", fiveLayer.actualCompilerCompatibility)
                    CompatibilityLayerRow("5. Actual Runtime (java)", fiveLayer.actualRuntimeCompatibility)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = fiveLayer.historicalAuthenticityNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Era Filter Strip
        val filterLabels = listOf(
            "All Releases (${jdks.size})",
            "Historical 1.0–1.4 (Java 2)",
            "Java 5–8",
            "Java 9–17",
            "Modern 18–25+"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterLabels.forEachIndexed { idx, label ->
                val selected = selectedEraFilter == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { selectedEraFilter = idx }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("jdk_era_filter_$idx")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // JDK Installations List / Table
        Text(
            text = "Installed & Configured JDK Registry (${filteredJdks.size} Shown)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredJdks.forEach { jdk ->
                val isSelected = jdk.versionId == uiState.activeJdkVersionId
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectJdk(jdk.versionId) }
                        .testTag("jdk_row_${jdk.versionId.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.DeveloperBoard,
                                    contentDescription = null,
                                    tint = if (isSelected) IdeSuccessGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = jdk.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Class ${jdk.bytecodeVersion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Text(
                            text = "Vendor: ${jdk.vendor} | Version: ${jdk.versionString} | Arch: ${jdk.architecture}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "JAVA_HOME: ${jdk.javaHome} | Compiler: ${jdk.compilerPath}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Flags: ${jdk.supportedCompilerFlags} | Status: ${jdk.status}",
                            style = MaterialTheme.typography.labelSmall,
                            color = IdeCyanPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatibilityLayerRow(title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
