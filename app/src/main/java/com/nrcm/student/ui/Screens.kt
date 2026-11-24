package com.nrcm.student.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nrcm.student.PlannerStatus
import com.nrcm.student.Status
import com.nrcm.student.buildPlannerStatus
import com.nrcm.student.canSkip
import com.nrcm.student.data.*
import com.nrcm.student.determineStatus
import com.nrcm.student.mustAttend
import com.nrcm.student.whatIf
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// THEME & COLOR PALETTE
// ==========================================
object NrcmTheme {
    val Primary = Color(0xFF4F46E5)          // Indigo 600
    val PrimaryDark = Color(0xFF3730A3)      // Indigo 800
    val PrimaryLight = Color(0xFFEEF2FF)     // Indigo 50
    val AccentBlue = Color(0xFF2563EB)       // Blue 600 (College Web Header)
    val TablePurple = Color(0xFF312E81)      // Deep Indigo (Summary Table Header)

    val Background = Color(0xFFF8FAFC)       // Slate 50
    val CardBg = Color(0xFFFFFFFF)           // Pure White
    val SurfaceMuted = Color(0xFFF1F5F9)     // Slate 100
    val Border = Color(0xFFE2E8F0)           // Slate 200
    val BorderLight = Color(0xFFEEF2F6)      // Very light border

    val TextPrimary = Color(0xFF0F172A)      // Slate 900
    val TextSecondary = Color(0xFF64748B)    // Slate 500
    val TextMuted = Color(0xFF94A3B8)        // Slate 400

    val PresentGreen = Color(0xFF16A34A)     // Emerald 600 (SAFE)
    val PresentBg = Color(0xFFDCFCE7)        // Emerald 100
    val AbsentRed = Color(0xFFDC2626)        // Red 600 (BELOW)
    val AbsentBg = Color(0xFFFEE2E2)         // Red 100
    val WarningAmber = Color(0xFFD97706)     // Amber 600 (CLOSE)
    val WarningBg = Color(0xFFFEF3C7)        // Amber 100
    val SemesterRowBg = Color(0xFFE0F2FE)    // Sky 100 (Total Row highlight)
    val SuccessBannerBg = Color(0xFFE6FFFA)  // Teal 50
    val SuccessBannerBorder = Color(0xFF99F6E4)
    val SuccessBannerText = Color(0xFF0F766E)
}

// ==========================================
// 1. LOGIN SCREEN (Modern, Minimal & Premium)
// ==========================================
@Composable
fun LoginScreen(
    onLogin: (String, String, Boolean) -> Unit,
    error: String?,
    loading: Boolean,
    rememberMe: Boolean = false,
    savedRollNo: String? = null
) {
    var roll by remember { mutableStateOf(savedRollNo ?: "") }
    var pass by remember { mutableStateOf("") }
    var isRemembered by remember { mutableStateOf(rememberMe) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NrcmTheme.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Branding Icon & Card
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(12.dp, CircleShape, spotColor = NrcmTheme.Primary.copy(alpha = 0.3f))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(NrcmTheme.Primary, NrcmTheme.AccentBlue)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "NRCM STUDENT",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = NrcmTheme.TextPrimary
            )

            Text(
                text = "Attendance & Academic Portal",
                style = MaterialTheme.typography.bodyMedium,
                color = NrcmTheme.TextSecondary
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Login Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = NrcmTheme.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Roll Number Field
                    OutlinedTextField(
                        value = roll,
                        onValueChange = { roll = it },
                        label = { Text("Roll Number") },
                        placeholder = { Text("e.g. 25X01A6229") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Roll Number",
                                tint = NrcmTheme.Primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NrcmTheme.Primary,
                            unfocusedBorderColor = NrcmTheme.Border,
                            focusedLabelColor = NrcmTheme.Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = NrcmTheme.Primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Star else Icons.Default.Lock,
                                    contentDescription = "Toggle password visibility",
                                    tint = NrcmTheme.TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NrcmTheme.Primary,
                            unfocusedBorderColor = NrcmTheme.Border,
                            focusedLabelColor = NrcmTheme.Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember Me
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRemembered = !isRemembered }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isRemembered,
                            onCheckedChange = { isRemembered = it },
                            colors = CheckboxDefaults.colors(checkedColor = NrcmTheme.Primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Remember me on this device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NrcmTheme.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button
                    Button(
                        onClick = { onLogin(roll.trim(), pass, isRemembered) },
                        enabled = !loading && roll.isNotBlank() && pass.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NrcmTheme.Primary,
                            contentColor = Color.White,
                            disabledContainerColor = NrcmTheme.Primary.copy(alpha = 0.45f),
                            disabledContentColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(if (roll.isNotBlank() && pass.isNotBlank()) 6.dp else 0.dp, RoundedCornerShape(14.dp), spotColor = NrcmTheme.Primary.copy(alpha = 0.4f))
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Error Message Banner
                    if (error != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NrcmTheme.AbsentBg),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = NrcmTheme.AbsentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = NrcmTheme.AbsentRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// STAT CARD WITH BLUE LEFT ACCENT (From Web UI)
// ==========================================
@Composable
fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(12.dp), spotColor = Color(0x10000000)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left blue vertical accent line (signature element from website screenshots)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        color = NrcmTheme.AccentBlue,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = NrcmTheme.TextSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = valueColor,
                    maxLines = 1
                )
            }
        }
    }
}

// ==========================================
// 2. MONTHS / ATTENDANCE PAGE (Exact Table)
// ==========================================
@Composable
fun MonthsTab(
    months: List<MonthRecord>,
    onRefresh: () -> Unit
) {
    if (months.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No attendance records found.",
                color = NrcmTheme.TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    // Default to the latest month in the list
    val defaultIndex = (months.size - 1).coerceAtLeast(0)
    var selectedIndex by remember(months) { mutableStateOf(defaultIndex) }
    val currentMonth = months.getOrNull(selectedIndex) ?: months.last()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NrcmTheme.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Heading Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF3B82F6), NrcmTheme.AccentBlue, NrcmTheme.PrimaryDark)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Semester Attendance (Month-wise)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "View your attendance records organized by month",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Month Selection Tabs with Badges (Only from parsed div.tab-pane, no hardcoded months)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            months.forEachIndexed { idx, m ->
                val isSelected = idx == selectedIndex
                val isGood = m.percent >= 75f
                val badgeBg = if (isGood) NrcmTheme.PresentGreen else NrcmTheme.WarningAmber
                val displayName = m.name.replace("-", " ")

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NrcmTheme.PrimaryLight else NrcmTheme.CardBg
                    ),
                    modifier = Modifier
                        .clickable { selectedIndex = idx }
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) NrcmTheme.Primary else NrcmTheme.Border,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) NrcmTheme.Primary else NrcmTheme.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(badgeBg, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f%%", m.percent),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top 3 Stats Cards (Present | Total Classes | Percentage)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Present",
                value = "${currentMonth.present}",
                valueColor = NrcmTheme.PresentGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Total Classes",
                value = "${currentMonth.total}",
                valueColor = NrcmTheme.AccentBlue,
                modifier = Modifier.weight(1f)
            )
            val pctColor = if (currentMonth.percent >= 75f) NrcmTheme.PresentGreen else NrcmTheme.WarningAmber
            StatCard(
                label = "Percentage",
                value = String.format(Locale.US, "%.2f%%", currentMonth.percent),
                valueColor = pctColor,
                modifier = Modifier.weight(1.1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date-wise Attendance Table (EXACT Website Structure: Date | P1 | P2 | P3 | P4 | P5 | P6)
        // With newest date first
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val tableScrollState = rememberScrollState()
                Box(modifier = Modifier.horizontalScroll(tableScrollState)) {
                    Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                        // Table Header Row
                        Row(
                            modifier = Modifier
                                .background(NrcmTheme.AccentBlue)
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(120.dp)
                            )
                            listOf("P1", "P2", "P3", "P4", "P5", "P6").forEach { pName ->
                                Text(
                                    text = pName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(44.dp)
                                )
                            }
                        }

                        // Table Body Rows: Newest Date First
                        val sortedDays = remember(currentMonth.days) {
                            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                            currentMonth.days.sortedWith(Comparator { a, b ->
                                val dateA = try { sdf.parse(a.date) } catch (e: Exception) { null }
                                val dateB = try { sdf.parse(b.date) } catch (e: Exception) { null }
                                when {
                                    dateA != null && dateB != null -> dateB.compareTo(dateA) // Newest first
                                    else -> 0
                                }
                            })
                        }

                        if (sortedDays.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No attendance records for this month", color = NrcmTheme.TextSecondary)
                            }
                        } else {
                            sortedDays.forEachIndexed { rowIdx, day ->
                                val rowBg = if (rowIdx % 2 == 0) NrcmTheme.CardBg else NrcmTheme.SurfaceMuted
                                Row(
                                    modifier = Modifier
                                        .background(rowBg)
                                        .border(width = 0.5.dp, color = NrcmTheme.Border)
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Date Cell
                                    Column(
                                        modifier = Modifier.width(120.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = day.date,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = NrcmTheme.TextPrimary
                                        )
                                        Text(
                                            text = day.dayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NrcmTheme.TextSecondary
                                        )
                                    }

                                    // P1 .. P6 Cells
                                    for (pIdx in 0 until 6) {
                                        val status = day.periods.getOrNull(pIdx) ?: PeriodStatus.NOT_POSTED
                                        val (text, color) = when (status) {
                                            PeriodStatus.PRESENT -> "P" to NrcmTheme.PresentGreen
                                            PeriodStatus.ABSENT -> "A" to NrcmTheme.AbsentRed
                                            PeriodStatus.NOT_POSTED -> "-" to NrcmTheme.TextMuted
                                        }

                                        Box(
                                            modifier = Modifier.width(44.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = text,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = color
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 3. MONTHLY SUMMARY & 4. SEMESTER SUMMARY
// ==========================================
@Composable
fun SummaryTab(
    months: List<MonthRecord>,
    semester: SemesterSummary,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NrcmTheme.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Monthly Summary Table Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Table Title Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(NrcmTheme.TablePurple, NrcmTheme.PrimaryDark)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Monthly Attendance Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                // Table Columns: Month | Classes Attended | Total Classes | Percentage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NrcmTheme.TablePurple)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Month",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        text = "Classes Attended",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.3f)
                    )
                    Text(
                        text = "Total Classes",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = "Percentage",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                // Monthly Rows
                months.forEachIndexed { idx, m ->
                    val rowBg = if (idx % 2 == 0) NrcmTheme.CardBg else NrcmTheme.SurfaceMuted
                    val pctColor = if (m.percent >= 75f) NrcmTheme.PresentGreen else NrcmTheme.WarningAmber
                    val displayName = m.name.replace("-", " ")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .border(width = 0.5.dp, color = NrcmTheme.BorderLight)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = NrcmTheme.TextPrimary,
                            modifier = Modifier.weight(1.8f)
                        )
                        Text(
                            text = "${m.present}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NrcmTheme.TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.3f)
                        )
                        Text(
                            text = "${m.total}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NrcmTheme.TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f%%", m.percent),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = pctColor,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }

                // Semester Total Row
                val semColor = if (semester.percent >= 75f) NrcmTheme.PresentGreen else NrcmTheme.AbsentRed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NrcmTheme.SemesterRowBg)
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Semester Total",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = NrcmTheme.TextPrimary,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        text = "${semester.present}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = NrcmTheme.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.3f)
                    )
                    Text(
                        text = "${semester.total}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = NrcmTheme.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f%%", semester.percent),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = semColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Semester Summary Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = NrcmTheme.Primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Semester Summary",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = NrcmTheme.TextPrimary
            )
        }

        // Three Summary Cards (Total Present | Total Classes | Overall Percentage)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = "Total Present",
                value = "${semester.present}",
                valueColor = NrcmTheme.PresentGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Total Classes",
                value = "${semester.total}",
                valueColor = NrcmTheme.AccentBlue,
                modifier = Modifier.weight(1f)
            )
            val overallColor = if (semester.percent >= 75f) NrcmTheme.PresentGreen else NrcmTheme.AbsentRed
            StatCard(
                label = "Overall %",
                value = String.format(Locale.US, "%.2f%%", semester.percent),
                valueColor = overallColor,
                modifier = Modifier.weight(1.1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Attendance Status Banner Message
        val isGoodAttendance = semester.percent >= 75f
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isGoodAttendance) NrcmTheme.SuccessBannerBorder else NrcmTheme.AbsentBg,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isGoodAttendance) NrcmTheme.SuccessBannerBg else NrcmTheme.AbsentBg
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGoodAttendance) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGoodAttendance) NrcmTheme.PresentGreen else NrcmTheme.AbsentRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isGoodAttendance) {
                        "Great job! You're maintaining good attendance. Keep up the good work!"
                    } else {
                        "Warning: Your attendance is below 75%. Attend classes regularly to get back on track!"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isGoodAttendance) NrcmTheme.SuccessBannerText else NrcmTheme.AbsentRed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// 6. PLANNER PAGE
// ==========================================
@Composable
fun MemeArea(status: Status, showMemes: Boolean = true) {
    if (!showMemes) return

    val memeText = when (status) {
        Status.SAFE -> "SAFE: Chill mode activated 🏖️"
        Status.CLOSE -> "CLOSE: Danger zone ⚠️"
        Status.BELOW -> "BELOW: Time to run to class 🏃‍♂️"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NrcmTheme.SurfaceMuted)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "[Meme Slot: $memeText]",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
                color = NrcmTheme.TextSecondary
            )
        }
    }
}

@Composable
fun PlannerProgressRing(
    plannerStatus: PlannerStatus,
    total: Int,
    showMemes: Boolean = true
) {
    // Ring colour: green for SAFE, amber for CLOSE, red for BELOW
    val ringColor = when (plannerStatus.status) {
        Status.SAFE -> NrcmTheme.PresentGreen
        Status.CLOSE -> NrcmTheme.WarningAmber
        Status.BELOW -> NrcmTheme.AbsentRed
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Circular Gauge Card
        Card(
            modifier = Modifier
                .size(190.dp)
                .shadow(6.dp, CircleShape),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                CircularProgressIndicator(
                    progress = if (total == 0) 0f else (plannerStatus.percent / 100f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxSize(),
                    color = if (total == 0) NrcmTheme.TextMuted else ringColor,
                    strokeWidth = 14.dp,
                    trackColor = (if (total == 0) NrcmTheme.TextMuted else ringColor).copy(alpha = 0.15f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (total == 0) "0.00%" else String.format(Locale.US, "%.2f%%", plannerStatus.percent),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = NrcmTheme.TextPrimary
                    )
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.labelSmall,
                        color = NrcmTheme.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Edge case: if total == 0, show no planner sentence
        if (total > 0) {
            val targetX = "${plannerStatus.targetPercent}%"

            when (plannerStatus.status) {
                Status.SAFE -> {
                    // SAFE: "You can skip N more classes and stay above X%."
                    Text(
                        text = "You can skip ${plannerStatus.canSkip} more classes and stay above $targetX.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        color = NrcmTheme.TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Status.CLOSE -> {
                    // CLOSE: "Careful! Only N classes left to skip before you drop below X%."
                    Text(
                        text = "Careful! Only ${plannerStatus.canSkip} classes left to skip before you drop below $targetX.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        color = NrcmTheme.TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Status.BELOW -> {
                    // BELOW: "Attend the next N classes in a row to get back to X%."
                    // Below that:
                    // "That's about D days (6 classes a day)"
                    // "If you attend every class, you'll reach X% around <date> (estimate, holidays are not included)."
                    // "After that you can skip M classes"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Attend the next ${plannerStatus.mustAttend} classes in a row to get back to $targetX.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = NrcmTheme.AbsentRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "That's about ${plannerStatus.daysNeeded} days (6 classes a day).",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center,
                            color = NrcmTheme.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "If you attend every class, you'll reach $targetX around ${plannerStatus.estimatedDate} (estimate, holidays are not included).",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = NrcmTheme.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "After that you can skip ${plannerStatus.afterAttendCanSkip} classes.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center,
                            color = NrcmTheme.PresentGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            MemeArea(status = plannerStatus.status, showMemes = showMemes)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerTab(
    months: List<MonthRecord>,
    semester: SemesterSummary,
    onRefresh: () -> Unit
) {
    var targetPercent by remember { mutableStateOf(75) }
    var skipN by remember { mutableStateOf(0) }
    var showMemes by remember { mutableStateOf(true) }

    val plannerStatus = buildPlannerStatus(semester.present, semester.total, targetPercent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NrcmTheme.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        PlannerProgressRing(plannerStatus, semester.total, showMemes)

        Spacer(modifier = Modifier.height(16.dp))

        // Target Percentage Selection (Never include 100)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Target Percentage",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = NrcmTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(65, 70, 75, 80, 85, 90).forEach { target ->
                        FilterChip(
                            selected = targetPercent == target,
                            onClick = { targetPercent = target },
                            label = { Text("$target%") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NrcmTheme.PrimaryLight,
                                selectedLabelColor = NrcmTheme.Primary
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // What-if Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What if I skip the next $skipN classes?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = NrcmTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = skipN.toFloat(),
                    onValueChange = { skipN = it.toInt() },
                    valueRange = 0f..30f,
                    steps = 29,
                    colors = SliderDefaults.colors(
                        thumbColor = NrcmTheme.Primary,
                        activeTrackColor = NrcmTheme.Primary
                    )
                )

                val whatIfPct = whatIf(semester.present, semester.total, skipN)
                val isBelow = whatIfPct < targetPercent
                val whatIfColor = if (isBelow) NrcmTheme.AbsentRed else NrcmTheme.PresentGreen

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Predicted New Attendance:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NrcmTheme.TextSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f%%", whatIfPct),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = whatIfColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Per-Month Breakdown (comes from parsed months)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Per-Month Breakdown",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = NrcmTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                months.forEachIndexed { idx, m ->
                    val mStatus = determineStatus(m.percent, targetPercent.toFloat())
                    val mColor = when (mStatus) {
                        Status.SAFE -> NrcmTheme.PresentGreen
                        Status.CLOSE -> NrcmTheme.WarningAmber
                        Status.BELOW -> NrcmTheme.AbsentRed
                    }
                    val displayName = m.name.replace("-", " ")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NrcmTheme.TextPrimary
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f%%", m.percent),
                            color = mColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (idx < months.size - 1) {
                        Divider(color = NrcmTheme.BorderLight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// TOP BAR & ABOUT DIALOG
// ==========================================
@Composable
fun AutoSizingTitle(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 20.sp,
    minFontSize: TextUnit = 14.sp
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val baseStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        color = NrcmTheme.TextPrimary
    )

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        var chosenSize = minFontSize

        for (size in maxFontSize.value.toInt() downTo minFontSize.value.toInt()) {
            val candidate = size.sp
            val measured = textMeasurer.measure(
                text = text,
                style = baseStyle.copy(fontSize = candidate),
                maxLines = 1
            )
            if (measured.size.width <= maxWidthPx) {
                chosenSize = candidate
                break
            }
            chosenSize = candidate
        }

        Text(
            text = text,
            style = baseStyle.copy(fontSize = chosenSize),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TopBar(
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onAbout: () -> Unit,
    studentName: String? = null,
    isRefreshing: Boolean = false,
    lastUpdated: Long? = null
) {
    val titleText = studentName?.takeIf { it.isNotBlank() } ?: "NRCM STUDENT"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = NrcmTheme.CardBg)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoSizingTitle(
                    text = titleText,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = NrcmTheme.TextSecondary
                        )
                    }
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isRefreshing) NrcmTheme.TextMuted else NrcmTheme.Primary
                        )
                    }
                    TextButton(onClick = onLogout) {
                        Text(
                            text = "Logout",
                            color = NrcmTheme.AbsentRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NrcmTheme.Primary,
                    trackColor = NrcmTheme.PrimaryLight
                )
            }
            if (lastUpdated != null && lastUpdated > 0) {
                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                Text(
                    text = "Last updated: ${sdf.format(Date(lastUpdated))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NrcmTheme.TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "NRCM Student Portal",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                "Modern attendance viewer and planner for Narsimha Reddy Engineering College students.\n\nYour login credentials are stored securely on your device and sent only to the official college portal.",
                style = MaterialTheme.typography.bodyMedium,
                color = NrcmTheme.TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NrcmTheme.Primary)
            ) {
                Text("Close")
            }
        }
    )
}

// ==========================================
// 5. DASHBOARD SCREEN (3 Tabs Only: Months, Summary, Planner)
// ==========================================
@Composable
fun DashboardScreen(
    months: List<MonthRecord>,
    semester: SemesterSummary,
    studentName: String? = null,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean = false,
    lastUpdated: Long? = null,
    refreshError: String? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAbout by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    Scaffold(
        topBar = {
            TopBar(
                onLogout = onLogout,
                onRefresh = onRefresh,
                onAbout = { showAbout = true },
                studentName = studentName,
                isRefreshing = isRefreshing,
                lastUpdated = lastUpdated
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NrcmTheme.CardBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Months") },
                    label = { Text("Months", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NrcmTheme.Primary,
                        selectedTextColor = NrcmTheme.Primary,
                        indicatorColor = NrcmTheme.PrimaryLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Summary") },
                    label = { Text("Summary", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NrcmTheme.Primary,
                        selectedTextColor = NrcmTheme.Primary,
                        indicatorColor = NrcmTheme.PrimaryLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Planner") },
                    label = { Text("Planner", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NrcmTheme.Primary,
                        selectedTextColor = NrcmTheme.Primary,
                        indicatorColor = NrcmTheme.PrimaryLight
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(NrcmTheme.Background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Refresh error banner with Retry button
                if (refreshError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NrcmTheme.AbsentBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = NrcmTheme.AbsentRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = refreshError,
                                    color = NrcmTheme.AbsentRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                            TextButton(
                                onClick = onRefresh,
                                colors = ButtonDefaults.textButtonColors(contentColor = NrcmTheme.Primary)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> MonthsTab(months = months, onRefresh = onRefresh)
                        1 -> SummaryTab(months = months, semester = semester, onRefresh = onRefresh)
                        2 -> PlannerTab(months = months, semester = semester, onRefresh = onRefresh)
                    }
                }
            }
        }
    }
}
