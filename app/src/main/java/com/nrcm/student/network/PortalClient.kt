package com.nrcm.student.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.nrcm.student.data.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class PortalClient {
    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val http = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun loginAndFetch(rollNo: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            val form = FormBody.Builder()
                .add("roll_no", rollNo)
                .add("password", password)
                .build()
            val loginReq = Request.Builder().url("https://www.nrcmec.org/Student/login.php").post(form).build()
            http.newCall(loginReq).execute().close()

            val attReq = Request.Builder().url("https://www.nrcmec.org/Student/Date_wise_attendance").build()
            val resp = http.newCall(attReq).execute()
            val html = resp.body?.string() ?: ""
            resp.close()

            val doc = Jsoup.parse(html)
            if (doc.getElementById("monthTabsContent") == null) return@withContext LoginResult.WrongCredentials
            val (months, sem) = parseAttendance(doc)
            LoginResult.Success(months, sem)
        } catch (e: IOException) { LoginResult.NetworkError }
        catch (e: Exception) { LoginResult.NetworkError }
    }

    private fun parseAttendance(doc: Document): Pair<List<MonthRecord>, SemesterSummary> {
        val container = doc.getElementById("monthTabsContent") ?: return emptyList<MonthRecord>() to SemesterSummary(0,0,0f,0,0,false)
        val panes = container.select("div.tab-pane")
        val months = mutableListOf<MonthRecord>()
        var countedPresentAll = 0
        var countedTotalAll = 0
        panes.forEach { pane ->
            val name = pane.id()
            val rows = pane.selectFirst("table.attendance-table")?.select("tbody tr") ?: return@forEach
            val days = mutableListOf<DayRecord>()
            var present = 0
            var total = 0
            rows.forEach { row ->
                val tds = row.select("td")
                if (tds.size < 2) return@forEach
                val date = tds[0].selectFirst("strong")?.text()?.trim() ?: ""
                val day = tds[0].selectFirst("small")?.text()?.trim() ?: ""
                val periods = mutableListOf<PeriodStatus>()
                for (i in 1 until tds.size) {
                    val cls = tds[i].className().lowercase()
                    when {
                        cls.contains("present") -> { periods.add(PeriodStatus.PRESENT); present++; total++ }
                        cls.contains("absent") -> { periods.add(PeriodStatus.ABSENT); total++ }
                        else -> periods.add(PeriodStatus.NOT_POSTED)
                    }
                }
                days.add(DayRecord(date, day, periods))
            }
            countedPresentAll += present
            countedTotalAll += total
            months.add(MonthRecord(name, days, present, total, if (total>0) present.toFloat()/total*100 else 0f, -1, -1, true))
        }

        val summary = doc.selectFirst("table.summary-table")
        var sumP = 0; var sumT = 0; var sumPct = 0f
        var matches = true
        if (summary != null) {
            val sRows = summary.select("tbody tr")
            if (sRows.isNotEmpty()) {
                val cells = sRows.last().select("td")
                if (cells.size >= 4) {
                    sumP = cells[1].text().trim().toIntOrNull() ?: 0
                    sumT = cells[2].text().trim().toIntOrNull() ?: 0
                    sumPct = cells[3].text().replace("%","").trim().toFloatOrNull() ?: 0f
                }
            }
            val monthRows = if (sRows.isNotEmpty()) sRows.dropLast(1) else emptyList()
            val updated = months.mapIndexed { idx, m ->
                if (idx < monthRows.size) {
                    val c = monthRows[idx].select("td")
                    val sp = c.getOrNull(1)?.text()?.trim()?.toIntOrNull() ?: -1
                    val st = c.getOrNull(2)?.text()?.trim()?.toIntOrNull() ?: -1
                    val match = m.present == sp && m.total == st
                    if (!match) matches = false
                    m.copy(summaryPresent=sp, summaryTotal=st, matches=match)
                } else m
            }
            return updated to SemesterSummary(sumP, sumT, sumPct, countedPresentAll, countedTotalAll, matches && sumP == countedPresentAll && sumT == countedTotalAll)
        }
        return months to SemesterSummary(0,0,0f, countedPresentAll, countedTotalAll, false)
    }
}