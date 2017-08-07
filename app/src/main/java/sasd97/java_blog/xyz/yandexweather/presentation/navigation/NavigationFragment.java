package sasd97.java_blog.xyz.yandexweather.presentation.navigation;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import sasd97.java_blog.xyz.yandexweather.R;
import sasd97.java_blog.xyz.yandexweather.presentation.main.MainActivity;
import sasd97.java_blog.xyz.yandexweather.presentation.main.MainPresenter;
import sasd97.java_blog.xyz.yandexweather.utils.AndroidMath;
import sasd97.java_blog.xyz.yandexweather.utils.ElevationScrollListener;

/**
 * Created by alexander on 09/07/2017.
 */

public class NavigationFragment extends MvpAppCompatFragment implements NavigationView {

    @BindView(R.id.fragment_navigation_recycler_cities) RecyclerView placesRecycler;
    @BindView(R.id.fragment_navigation_nav_view) RelativeLayout navigationView;
    @BindView(R.id.fragment_navigation_settings_image) ImageView navSettingsImage;
    @BindView(R.id.fragment_navigation_settings) TextView navSettings;
    @BindView(R.id.fragment_navigation_about) ImageView navAbout;
    @BindBool(R.bool.is_tablet) boolean isTablet;
    @BindBool(R.bool.is_tablet_vertical) boolean isTabletVertical;

    private PlacesRecyclerAdapter placesRecyclerAdapter;
    private RecyclerView.OnScrollListener onScrollListener;
    private LinearLayoutManager layoutManager;
    private int actionBarHeight;

    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navigation, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initActionBarHeight();
        if (!isTabletVertical) initOnNotTabletVertical();
        layoutManager = new LinearLayoutManager(getActivity());
        placesRecycler.setLayoutManager(layoutManager);
        placesRecyclerAdapter = new PlacesRecyclerAdapter();
        placesRecycler.setAdapter(placesRecyclerAdapter);
        placesRecycler.setHasFixedSize(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) initOnScrollListener();
        initNavigation();
    }

    private void initNavigation() {
        MainPresenter presenter = ((MainActivity) getActivity()).mainPresenter;
        navAbout.setOnClickListener(v -> presenter.navigateWeatherTo(R.id.main_activity_navigation_about));
        navSettings.setOnClickListener(v -> presenter.navigateWeatherTo(R.id.main_activity_navigation_settings));
        navSettingsImage.setOnClickListener(v -> presenter.navigateWeatherTo(R.id.main_activity_navigation_settings));
    }

    private void initActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data, getResources().getDisplayMetrics());
        }
    }

    private void initOnNotTabletVertical() {
        navigationView.setTranslationY(actionBarHeight);
    }

    public void makeNavigationView(float slideOffset) {
        navigationView.setTranslationY(actionBarHeight * slideOffset);
        navSettings.setAlpha(slideOffset * slideOffset);
        navSettingsImage.setAlpha(1 - slideOffset * 2);
    }

    private void initOnScrollListener() {
        int toolbarElevation = AndroidMath.dp2px(isTablet ? 4 : 0, getResources());
        int toolbarElevationBase = AndroidMath.dp2px(1, getResources());
        int navViewElevation = AndroidMath.dp2px(14, getResources());
        int navViewElevationBase = AndroidMath.dp2px(6, getResources());
        //// TODO: 8/6/2017 move to resources
        onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    ((ElevationScrollListener) getActivity()).onToolbarElevation(toolbarElevation);
                    onNavigationViewElevation(navViewElevation);
                } else {
                    if (isTablet) {
                        if (layoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                            ((ElevationScrollListener) getActivity()).onToolbarElevation(toolbarElevationBase);
                        } else {
                            ((ElevationScrollListener) getActivity()).onToolbarElevation(toolbarElevation);
                        }
                    }
                    if (layoutManager.findLastCompletelyVisibleItemPosition() ==
                            placesRecyclerAdapter.getItemCount() - 1) {
                        onNavigationViewElevation(navViewElevationBase);
                    }
                }
            }

            @Override
            public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };
        placesRecycler.addOnScrollListener(onScrollListener);
        onNavigationViewElevation(navViewElevationBase);
        ((ElevationScrollListener) getActivity()).onToolbarElevation(toolbarElevationBase);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (onScrollListener != null) placesRecycler.removeOnScrollListener(onScrollListener);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onNavigationViewElevation(int elevation) {
        navigationView.animate().translationZ(elevation);
    }
}