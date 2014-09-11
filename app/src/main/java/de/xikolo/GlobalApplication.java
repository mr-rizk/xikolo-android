package de.xikolo;

import android.app.Application;
import android.net.http.HttpResponseCache;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import java.io.File;
import java.io.IOException;

import de.xikolo.util.Config;
import de.xikolo.util.FontsOverride;
import de.xikolo.util.SslCertificateUtil;

public class GlobalApplication extends Application {

    public static final String TAG = GlobalApplication.class.getSimpleName();

    private static GlobalApplication instance;

    private JobManager jobManager;

    private HttpResponseCache httpResponseCache;

    private CookieSyncManager cookieSyncManager;

    public GlobalApplication() {
        instance = this;
    }

    public static GlobalApplication getInstance() {
        return instance;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configureFontTypefaces();
        configureImageLoader();
        configureHttpResponseCache();
        configureWebViewCookies();
        configureJobManager();

        if (Config.DEBUG)
            SslCertificateUtil.disableSslCertificateChecking();
    }

    private void configureFontTypefaces() {
        // Set global Typefaces
        FontsOverride.setDefaultFont(this, "SANS_SERIF", Config.FONT_SANS);
    }

    private void configureImageLoader() {
        // Create ImageLoader configuration
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .showImageOnLoading(R.color.gray_text)
                .showImageForEmptyUri(R.color.gray_text)
                .showImageOnFail(R.color.gray_text)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .discCacheSize(30 * 1024 * 1024) // 30 MiB
                .build();
        ImageLoader.getInstance().init(config);
    }

    private void configureHttpResponseCache() {
        // Create HTTP Response Cache
        try {
            File httpCacheDir = new File(this.getCacheDir(), "http");
            long httpCacheSize = 20 * 1024 * 1024; // 20 MiB
            httpResponseCache = HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException e) {
            Log.i(TAG, "HTTP Response Cache installation failed:" + e);
        }
    }

    private void configureWebViewCookies() {
        // Enable WebView Cookies
        cookieSyncManager = CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);
    }

    private void configureJobManager() {
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return Config.DEBUG;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1) // always keep at least one consumer alive
                .maxConsumerCount(3) // up to 3 consumers at a time
                .loadFactor(3) // 3 jobs per consumer
                .consumerKeepAlive(120) // wait 2 minute
                .build();
        jobManager = new JobManager(this, configuration);
    }

    public void flushHttpResponseCache() {
        if (httpResponseCache != null) {
            httpResponseCache.flush();
        }
    }

    public void startCookieSyncManager() {
        if (cookieSyncManager != null) {
            cookieSyncManager.startSync();
        }
    }

    public void stopCookieSyncManager() {
        if (cookieSyncManager != null) {
            cookieSyncManager.stopSync();
        }
    }

    public void syncCookieSyncManager() {
        if (cookieSyncManager != null) {
            cookieSyncManager.sync();
        }
    }

}