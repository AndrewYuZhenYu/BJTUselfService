package team.bjtuss.bjtuselfservice.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import team.bjtuss.bjtuselfservice.StudentAccountManager
import team.bjtuss.bjtuselfservice.utils.DownloadUtil

object OtherFunctionNetworkRepository {
    private val client = StudentAccountManager.getInstance().client

    suspend fun downloadGradeList(isEnglish: Boolean) {
        withContext(Dispatchers.IO) {
            var cookies = ""
            client.cookieJar.loadForRequest(
                Request.Builder().url("https://aa.bjtu.edu.cn").build().url
            )
                .forEach {
                    cookies += "${it.name}=${it.value};"
                }
            if (isEnglish) {
                DownloadUtil.downloadFile(
                    url =
                    "https://aa.bjtu.edu.cn/score/scorecard/stu/5201314/download_pdf/?type=card_en_sign&has_advance_query=",
                    title = "英文成绩单",
                    cookies
                )
            } else {
                DownloadUtil.downloadFile(
                    url = "https://aa.bjtu.edu.cn/score/scorecard/stu/5201314/download_pdf/?type=card_cn_sign&has_advance_query=",
                    title = "中文成绩单",
                    cookies
                )
            }
        }
    }

}
