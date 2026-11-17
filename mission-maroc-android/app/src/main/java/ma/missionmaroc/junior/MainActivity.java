package ma.missionmaroc.junior;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private TextToSpeech tts;
    private volatile boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#1E4FA8"));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom());
            return insets;
        });

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Voice(), "AndroidVoice");
        root.addView(webView);
        setContentView(root);

        tts = new TextToSpeech(this, status -> ttsReady = status == TextToSpeech.SUCCESS);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        webView.evaluateJavascript("window.appBack && window.appBack()", result -> {
            if (!"true".equals(result)) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        webView.destroy();
        super.onDestroy();
    }

    private class Voice {
        @JavascriptInterface
        public String speak(String text, String lang) {
            if (!ttsReady) {
                return "notready";
            }
            Locale locale;
            if ("ar".equals(lang)) {
                locale = new Locale("ar");
            } else if ("en".equals(lang)) {
                locale = Locale.UK;
            } else {
                locale = Locale.FRANCE;
            }
            if (tts.isLanguageAvailable(locale) < TextToSpeech.LANG_AVAILABLE) {
                return "novoice";
            }
            tts.setLanguage(locale);
            tts.setSpeechRate(0.9f);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "consigne");
            return "ok";
        }
    }
}
