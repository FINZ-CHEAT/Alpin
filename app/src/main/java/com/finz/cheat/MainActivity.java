package com.finz.cheat;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    private WebView web;

    private static final int REQUEST_CODE = 1001;

    private final Shizuku.OnRequestPermissionResultListener permissionListener =
        (requestCode, grantResult) -> {
            if (requestCode == REQUEST_CODE && web != null) {
                final boolean granted =
                    grantResult == PackageManager.PERMISSION_GRANTED;

                runOnUiThread(() -> web.evaluateJavascript(
                    "onShizukuPermissionResult(" + granted + ")", null
                ));
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Shizuku.addRequestPermissionResultListener(permissionListener);

        web = new WebView(this);
        web.setWebViewClient(new WebViewClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);

        web.addJavascriptInterface(new AndroidBridge(), "FinzAndroid");

        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    public class AndroidBridge {

        @JavascriptInterface
        public String getShizukuStatus() {
            if (!Shizuku.pingBinder()) {
                return "SHIZUKU TIDAK AKTIF";
            }

            try {
                if (Shizuku.checkSelfPermission()
                        == PackageManager.PERMISSION_GRANTED) {
                    return "SHIZUKU AKTIF - IZIN DIBERIKAN";
                }

                return "SHIZUKU AKTIF - IZIN BELUM DIBERIKAN";

            } catch (Exception e) {
                return "GAGAL MEMERIKSA IZIN";
            }
        }

        @JavascriptInterface
        public String requestShizukuPermission() {
            if (!Shizuku.pingBinder()) {
                return "Aktifkan Shizuku terlebih dahulu";
            }

            if (Shizuku.checkSelfPermission()
                    == PackageManager.PERMISSION_GRANTED) {
                return "Izin Shizuku sudah diberikan";
            }

            Shizuku.requestPermission(REQUEST_CODE);
            return "Permintaan izin dikirim";
        }

        @JavascriptInterface
        public String launchGame(String packageName) {
            if (!packageName.equals("com.dts.freefireth")
                    && !packageName.equals("com.dts.freefiremax")) {
                return "Paket aplikasi tidak diizinkan";
            }

            try {
                Intent intent = getPackageManager()
                        .getLaunchIntentForPackage(packageName);

                if (intent == null) {
                    return "Game belum terinstal";
                }

                startActivity(intent);
                return "Game berhasil dibuka";

            } catch (Exception e) {
                return "Gagal membuka game";
            }
        }
    }

    @Override
    protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener);

        if (web != null) {
            web.removeJavascriptInterface("FinzAndroid");
            web.destroy();
        }

        super.onDestroy();
    }
}
