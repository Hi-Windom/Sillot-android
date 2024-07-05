package sc.windom.sofill.Us

import android.webkit.ValueCallback
import android.webkit.WebView

object U_Webview {
}

fun WebView.injectVConsole(resultCallback: ValueCallback<String?>? = null) {
    val js = """
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.src = 'https://unpkg.com/vconsole@latest/dist/vconsole.min.js';
        document.head.appendChild(script);
        script.onload = function() {
            var vConsole = new window.VConsole();
            vConsole.showSwitch();
        };
"""
    this.evaluateJavascript(js, resultCallback)
}