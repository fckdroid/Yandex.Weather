package sasd97.java_blog.xyz.yandexweather.domain.weather;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import sasd97.java_blog.xyz.yandexweather.data.AppRepository;
import sasd97.java_blog.xyz.yandexweather.data.models.forecast.ResponseForecast;
import sasd97.java_blog.xyz.yandexweather.data.models.places.Place;
import sasd97.java_blog.xyz.yandexweather.domain.converters.Converter;
import sasd97.java_blog.xyz.yandexweather.domain.models.WeatherModel;

import static sasd97.java_blog.xyz.yandexweather.domain.converters.ConvertersConfig.PRESSURE_CONVERTERS_KEY;
import static sasd97.java_blog.xyz.yandexweather.domain.converters.ConvertersConfig.SPEED_CONVERTERS_KEY;
import static sasd97.java_blog.xyz.yandexweather.domain.converters.ConvertersConfig.TEMPERATURE_CONVERTERS_KEY;

/**
 * Created by alexander on 12/07/2017.
 */

public class WeatherInteractorImpl implements WeatherInteractor {

    private static final String TAG = WeatherInteractorImpl.class.getCanonicalName();

    private Gson gson;
    private AppRepository repository;
    private List<Converter<Integer, Float>> speedConverters;
    private List<Converter<Integer, Float>> pressuresConverters;
    private List<Converter<Integer, Float>> temperatureConverters;

    public WeatherInteractorImpl(@NonNull Gson gson,
                                 @NonNull AppRepository repository,
                                 @NonNull Map<String, List<Converter<Integer, Float>>> converters) {
        this.gson = gson;
        this.repository = repository;
        this.speedConverters = converters.get(SPEED_CONVERTERS_KEY);
        this.pressuresConverters = converters.get(PRESSURE_CONVERTERS_KEY);
        this.temperatureConverters = converters.get(TEMPERATURE_CONVERTERS_KEY);
    }

    @Override
    public Observable<WeatherModel> getWeather(@NonNull Place place) {
        String cacheWeather = repository.getCachedWeather(place);
        if (cacheWeather == null) return updateWeather(place);
        Observable<WeatherModel> observable = Observable.just(cacheWeather)
                .map(cache -> gson.fromJson(cache, WeatherModel.class));

        return convertModel(observable);
    }

    @Override
    public Observable<WeatherModel> updateWeather(@NonNull Place place) {
        Observable<WeatherModel> observable = repository
                .getWeather(place)
                .doOnNext(w -> repository.saveWeatherToCache(place, gson.toJson(w)));
        return convertModel(observable);
    }

    @Override
    public Single<ResponseForecast> getForecast(@NonNull Place place) {
//        String cacheForecast = repository.getCachedWeather(place);
//        if (cacheForecast == null) return updateWeather(place);
//        Observable<WeatherModel> observable = Observable.just(cacheForecast)
//                .map(cache -> gson.fromJson(cache, WeatherModel.class));

//        return convertModel(observable);
        return null;
    }

    @Override
    public Single<ResponseForecast> updateForecast(@NonNull Place place) {
        return repository.getForecast(place);
    }


    @Override
    public int getTemperatureUnits() {
        return repository.getTemperatureUnits();
    }

    @Override
    public int getPressureUnits() {
        return repository.getPressureUnits();
    }

    @Override
    public int getSpeedUnits() {
        return repository.getSpeedUnits();
    }

    private Observable<WeatherModel> convertModel(Observable<WeatherModel> weatherObservable) {
        return weatherObservable
                .map(w -> new WeatherModel.Builder(w)
                        .temperature(
                                applyConverter(repository.getTemperatureUnits(),
                                        w.getTemperature(), temperatureConverters)
                        )
                        .minTemperature(
                                applyConverter(repository.getTemperatureUnits(),
                                        w.getMinTemperature(), temperatureConverters)
                        )
                        .maxTemperature(
                                applyConverter(repository.getTemperatureUnits(),
                                        w.getMaxTemperature(), temperatureConverters)
                        )
                        .pressure(
                                (int) applyConverter(repository.getPressureUnits(),
                                        w.getPressure(), pressuresConverters)
                        )
                        .windSpeed(
                                applyConverter(repository.getSpeedUnits(),
                                        w.getWindSpeed(), speedConverters)
                        )
                        .build());
    }

    private float applyConverter(int mode, float value,
                                 @NonNull List<Converter<Integer, Float>> converters) {
        for (Converter<Integer, Float> converter: converters) {
            if (converter.isApplicable(mode)) return converter.convert(value);
        }
        return value;
    }
}
