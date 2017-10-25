package sasd97.java_blog.xyz.yandexweather;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;
import com.jakewharton.threetenabp.AndroidThreeTen;

import javax.inject.Inject;

import sasd97.java_blog.xyz.richtextview.FontProvider;
import sasd97.java_blog.xyz.yandexweather.background.UpdateWeatherJob;
import sasd97.java_blog.xyz.yandexweather.data.AppRepository;
import sasd97.java_blog.xyz.yandexweather.di.AppComponent;
import sasd97.java_blog.xyz.yandexweather.di.DaggerAppComponent;
import sasd97.java_blog.xyz.yandexweather.di.MainComponent;
import sasd97.java_blog.xyz.yandexweather.di.modules.AppModule;
import sasd97.java_blog.xyz.yandexweather.di.modules.MainModule;
import sasd97.java_blog.xyz.yandexweather.di.modules.NavigationModule;
import timber.log.Timber;

/**
 * Created by alexander on 07/07/2017.
 */

public class WeatherApp extends Application {
    public static final String SPACE = " ";

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private AppComponent appComponent;
    private MainComponent mainComponent;

    @Inject JobManager jobManager;
    @Inject AppRepository repository;

    public static WeatherApp get(@NonNull Context context) {
        return (WeatherApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = buildAppComponent();
        getAppComponent().inject(this);
        onInit();
    }

    private void onInit() {
        FontProvider.init(getAssets());
        AndroidThreeTen.init(this);
        onScheduleJob();
        Timber.plant(new Timber.DebugTree());
        Stetho.initializeWithDefaults(this);
    }

    private void onScheduleJob() {
        if (!repository.isBackgroundServiceEnabled()) return;
        if (jobManager.getAllJobRequestsForTag(UpdateWeatherJob.TAG).size() > 0) return;
        UpdateWeatherJob.scheduleJob(repository.getWeatherUpdateInterval());
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public MainComponent getMainComponent() {
        if (mainComponent == null) mainComponent
                = appComponent.plusMainComponent(new MainModule());
        return mainComponent;
    }

    private AppComponent buildAppComponent() {
        return DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .navigationModule(new NavigationModule())
                .build();
    }
}
