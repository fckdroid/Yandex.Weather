package sasd97.java_blog.xyz.yandexweather.di;

import dagger.Subcomponent;
import sasd97.java_blog.xyz.yandexweather.di.modules.MainModule;
import sasd97.java_blog.xyz.yandexweather.di.scopes.MainScope;
import sasd97.java_blog.xyz.yandexweather.presentation.main.MainActivity;

/**
 * Created by alexander on 09/07/2017.
 */

@Subcomponent(modules = {MainModule.class})
@MainScope
public interface MainComponent {
    void inject(MainActivity activity);
}