package com.hrsnkwge.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import kotlinx.android.synthetic.main.browser.*


class Browser : AppCompatActivity(){
    companion object{
        lateinit var mywebView:WebView
        var current = ""
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser)
        mywebView = webView
        mywebView.apply {
            settings.apply {
                javaScriptEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                javaScriptCanOpenWindowsAutomatically = true
                databaseEnabled = true
                loadsImagesAutomatically = true
                loadWithOverviewMode = true
                useWideViewPort = true
                isFocusableInTouchMode = true
                setSupportMultipleWindows(true)
                setAppCacheEnabled(true)
                setSupportZoom(true)

                userAgentString += " TEST_USER_AGENT/" + BuildConfig.VERSION_NAME

                setOnKeyListener { view, keyCode, keyEvent ->
                    if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack() == true) {
                        webView.goBack()
                        return@setOnKeyListener true
                    }
                    return@setOnKeyListener false
                }

            }
        }
        mywebView.webViewClient = CustomWebViewClient(this)
        mywebView.webChromeClient = CustomWebChromeClient(this)
        mywebView.loadUrl("https://wsdmoodle.waseda.jp/my/")


    }


    // WebViewClient のカスタムクラス
    private class CustomWebViewClient internal constructor(private val activity: Browser) :
        WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            if (view != null) {
                if(!current.equals("0"))mywebView.loadUrl("javascript:(function() { " +"console.log(\$('.w-25.bg-pulse-grey:visible').length);"+ "})()")
            }
        }

        // shouldOverrideUrlLoading() では反応しません
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)

            Log.d("TEST", "activity.webView.url:" + activity.webView.url)

            // *** ここでクリックイベントを補足して任意の処理を実行させる *** //
        }
    }

    // WebChromeClient のカスタムクラス
    private class CustomWebChromeClient internal constructor(private val activity: Browser) :
        WebChromeClient() {

        // JavaScript を動かすために必要です
        override fun onJsAlert(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            return false
        }

        // JavaScript を動かすために必要です
        override fun onJsConfirm(
            view: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            view ?: return true
            MaterialDialog(view.context).show {
                title(text = message)
                positiveButton {
                    result?.confirm()
                }
                negativeButton {
                    result?.cancel()
                }
                onCancel {
                    result?.cancel()
                }
            }

            return true
        }

        // JavaScript を動かすために必要です
        override fun onJsPrompt(
            view: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            return false
        }


        // <a target="_blank"> を反応させるために必要です
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {

            view ?: return false

            val href = view.handler.obtainMessage()
            view.requestFocusNodeHref(href)
            val url = href.data.getString("url")

            view.stopLoading()
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(browserIntent)
            return true
        }

        var xhrnum = 0
        var last = "false"

        // console.log を Logcat に表示させます
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            if (consoleMessage != null) {
                Log.d(
                    "TEST",
                    "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + " - " + consoleMessage.sourceId() + "：" + consoleMessage.lineNumber() + "行目"
                )
                current = consoleMessage.message()
                if(!current.equals("0"))mywebView.loadUrl("javascript:(function() { " +"console.log(\$('.w-25.bg-pulse-grey:visible').length);"+ "})()")
                else if(current.equals("0")&&!last.equals("true")){
                    Log.d("Clicked","clicked")
                    mywebView.loadUrl("javascript:(function() { " +"console.log(\$('.w-25.bg-pulse-grey:visible').length);"+ "})()")
                    mywebView.loadUrl("javascript:(function() { " +"console.log(\$('li[data-control=\"next\"]').find('.page-link').eq(1).parent().hasClass('disabled'));"+ "})()")
                    mywebView.loadUrl("javascript:(function() { " +"\$('li[data-control=\"next\"]').find('.page-link')[1].click()"+ "})()")
                }else if(current.equals("true")){
                    last = "true"
                }

            }
            return super.onConsoleMessage(consoleMessage)
        }
    }


}