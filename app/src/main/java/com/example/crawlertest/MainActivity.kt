package com.example.crawlertest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val baseUrl = "https://onepiece.nchu.edu.tw/cofsys/plsql/crseqry_gene_now"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val html = fetchData(baseUrl)
                val document = Jsoup.parse(html)

                // Select an option in the select tag
                val selectOptionValue = "F" // Replace with the desired option value
                val selectElement = document.selectFirst("select[name=p_subject]")
                selectElement?.select("option")?.forEach { option ->
                    if (option.`val`() == selectOptionValue) {
                        option.attr("selected", "")
                    } else {
                        option.removeAttr("selected")
                    }
                }

                // Extract the form action and method
                val formAction = document.selectFirst("form[name=form2]")?.attr("action")
                val formMethod = document.selectFirst("form[name=form2]")?.attr("method")

                // Construct the form data
                val formData = mutableMapOf<String, String>()
                val elements = document.select("table")
                for (element in elements) {
                    val name = element.attr("name")
                    val value = element.attr("value")
                    formData[name] = value
                }

                // Add any additional form data if needed
                formData["additionalParam"] = "value"

                // Submit the form
                val responseHtml = formMethod?.let {
                    submitForm(baseUrl + formAction,
                        it, formData)
                }

                val responseDocument = Jsoup.parse(responseHtml)

                // Crawling the text content of tables with the name "word_13"
                val tables = responseDocument.select("table[name=word_13]")

                for (table in tables) {
                    val tableText = table.text()
                    Log.d(TAG, "Table Content: $tableText")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    private fun fetchData(url: String): String {
        val okHttpClient = OkHttpClient().newBuilder().build()

        val request: Request = Request.Builder().url(url).get().build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()

        return response.body()?.string() ?: ""
    }

    private fun submitForm(url: String, method: String, formData: Map<String, String>): String {
        val okHttpClient = OkHttpClient().newBuilder().build()

        val requestBody = FormBody.Builder().apply {
            for ((name, value) in formData) {
                add(name, value)
            }
        }.build()

        val request = Request.Builder().apply {
            url(url)
            method(method, requestBody)
        }.build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()

        return response.body()?.string() ?: ""
    }
}

