package com.byagowi.persiancalendar.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.adapter.DrawerAdapter;
import com.byagowi.persiancalendar.push.MyFirebaseMessagingService;
import com.byagowi.persiancalendar.service.AthanNotification;
import com.byagowi.persiancalendar.util.TypeFaceUtil;
import com.byagowi.persiancalendar.util.UpdateUtils;
import com.byagowi.persiancalendar.util.Utils;
import com.byagowi.persiancalendar.view.fragment.AboutFragment;
import com.byagowi.persiancalendar.view.fragment.ApplicationPreferenceFragment;
import com.byagowi.persiancalendar.view.fragment.CalendarFragment;
import com.byagowi.persiancalendar.view.fragment.CompassFragment;
import com.byagowi.persiancalendar.view.fragment.ConverterFragment;
/*import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;*/
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pushpole.sdk.PushPole;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import calendar.CivilDate;

import static com.byagowi.persiancalendar.Constants.DEFAULT_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.LANG_EN_US;
import static com.byagowi.persiancalendar.Constants.LANG_UR;
import static com.byagowi.persiancalendar.Constants.PREF_APP_LANGUAGE;
import static com.byagowi.persiancalendar.Constants.PREF_PERSIAN_DIGITS;
import static com.byagowi.persiancalendar.Constants.PREF_SHOW_DEVICE_CALENDAR_EVENTS;
import static com.byagowi.persiancalendar.Constants.PREF_THEME;

/**
 * Program activity for android
 *
 * @author ebraminio
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

   /* AdView adView;
    InterstitialAd mInterstitialAd;*/
    SharedPreferences pref;

    private static final int CALENDAR = 1;
    private static final int CONVERTER = 2;
    private static final int COMPASS = 3;
    public static final int PREFERENCE = 4;
    //private static final int ABOUT = 5;
    private static final int EXIT = 5;
    // Default selected fragment
    private static final int DEFAULT = CALENDAR;
    private final String TAG = MainActivity.class.getName();
    private DrawerLayout drawerLayout;
    private DrawerAdapter adapter;
    private Class<?>[] fragments = {
            null,
            CalendarFragment.class,
            ConverterFragment.class,
            CompassFragment.class,
            ApplicationPreferenceFragment.class,
         //   AboutFragment.class
    };
    private int menuPosition = 0; // it should be zero otherwise #selectItem won't be called

    // https://stackoverflow.com/a/3410200
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void oneTimeClockDisablingForAndroid5LE() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            String key = "oneTimeClockDisablingForAndroid5LE";
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!prefs.getBoolean(key, false)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(Constants.PREF_WIDGET_CLOCK, false);
                edit.putBoolean(key, true);
                edit.apply();
            }
        }
    }

    private static CivilDate creationDate;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setTheme(this);
        Utils.changeAppLanguage(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Utils.initUtils(this);
        TypeFaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/NotoNaskhArabic-Regular.ttf"); // font from assets: "assets/fonts/Roboto-Regular.ttf

        Utils.startEitherServiceOrWorker(this);

        // Doesn't matter apparently
        // oneTimeClockDisablingForAndroid5LE();
        UpdateUtils.update(getApplicationContext(), false);
        PushPole.initialize(this, true);
        setContentView(R.layout.activity_main);

/* MobileAds.initialize(this, "ca-app-pub-8013310084549686~3416894852");
        adView = (AdView)findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().build();
      //  AdRequest adRequest = new AdRequest.Builder().addTestDevice("C8A60CCC5FEBE6D5BC2B3F6BCEB637A8").build();
        adView.loadAd(adRequest);
        adView.setVisibility(View.VISIBLE);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8013310084549686/7112597285");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());*/
     //   mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("C8A60CCC5FEBE6D5BC2B3F6BCEB637A8").build());
   /*     handler = new Handler();

        final Runnable r = () -> {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                Log.d("Ads", "showAd");
/*     handler = new Handler();

        final Runnable r = () -> {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                Log.d("Ads", "showAd");

            } else {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
              //  mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("C8A60CCC5FEBE6D5BC2B3F6BCEB637A8").build());
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                //     AdadFullscreenBannerAd.show(MainActivity.this);

                Log.e("TAG", "The interstitial wasn't loaded yet.");
                //  Toast.makeText(MainActivity.this,"The interstitial wasn't loaded yet.",Toast.LENGTH_LONG).show();
            }
       ///     handler.postDelayed(this, 5000);
        };
        handler.postDelayed(r, 5000);*/
        //    } else {
           //     mInterstitialAd.loadAd(new AdRequest.Builder().build());
          /*    //  mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("C8A60CCC5FEBE6D5BC2B3F6BCEB637A8").build());
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                //     AdadFullscreenBannerAd.show(MainActivity.this);

                Log.e("TAG", "The interstitial wasn't loaded yet.");
                //  Toast.makeText(MainActivity.this,"The interstitial wasn't loaded yet.",Toast.LENGTH_LONG).show();
            }
       ///     handler.postDelayed(this, 5000);
        };*/
     //   handler.postDelayed(r, 5000);*/

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("TAG", msg);
                        //  Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().subscribeToTopic("All");


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        }

        RecyclerView navigation = findViewById(R.id.navigation_view);
        navigation.setHasFixedSize(true);
        adapter = new DrawerAdapter(this);
        navigation.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        navigation.setLayoutManager(layoutManager);

        drawerLayout = findViewById(R.id.drawer);
        final View appMainView = findViewById(R.id.app_main_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            int slidingDirection = +1;

            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    if (Utils.isRTL(getApplicationContext()))
                        slidingDirection = -1;
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                slidingAnimation(drawerView, slideOffset);
            }


            private void slidingAnimation(View drawerView, float slideOffset) {
                appMainView.setTranslationX(slideOffset * drawerView.getWidth() * slidingDirection);
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }
        };

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        String action = getIntent() != null ? getIntent().getAction() : null;
        if ("COMPASS_SHORTCUT".equals(action)) {
            selectItem(COMPASS);
        } else if ("PREFERENCE_SHORTCUT".equals(action)) {
            selectItem(PREFERENCE);
        } else if ("CONVERTER_SHORTCUT".equals(action)) {
            selectItem(CONVERTER);
        }/* else if ("ABOUT_SHORTCUT".equals(action)) {
            selectItem(ABOUT);
        }*/ else {
            selectItem(DEFAULT);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        if (Utils.isShowDeviceCalendarEvents()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                Utils.askForCalendarPermission(this);
            }
        }

        creationDate = Utils.getGregorianToday();
        Utils.changeAppLanguage(this);
    }

    boolean settingHasChanged = false;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        settingHasChanged = true;
        if (key.equals(PREF_APP_LANGUAGE)) {
            boolean persianDigits;
            switch (sharedPreferences.getString(PREF_APP_LANGUAGE, DEFAULT_APP_LANGUAGE)) {
                case LANG_EN_US:
                    persianDigits = false;
                    break;
                case LANG_UR:
                    persianDigits = false;
                    break;
                default:
                    persianDigits = true;
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_PERSIAN_DIGITS, persianDigits);
            editor.apply();
        }

        if (key.equals(PREF_SHOW_DEVICE_CALENDAR_EVENTS)) {
            if (sharedPreferences.getBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, true)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    Utils.askForCalendarPermission(this);
                }
            }
        }

        if (key.equals(PREF_APP_LANGUAGE) || key.equals(PREF_THEME)) {
            restartActivity(PREFERENCE);
        }

        UpdateUtils.update(getApplicationContext(), true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CALENDAR_READ_PERMISSION_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                Utils.initUtils(this);
                if (menuPosition == CALENDAR) {
                    restartActivity(menuPosition);
                }
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false);
                edit.apply();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.initUtils(this);
        View v = findViewById(R.id.drawer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            v.setLayoutDirection(Utils.isRTL(this) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.changeAppLanguage(this);
        UpdateUtils.update(getApplicationContext(), false);
        if (!creationDate.equals(Utils.getGregorianToday())) {
            restartActivity(menuPosition);
        }
    }


    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if (menuPosition != DEFAULT) {
            selectItem(DEFAULT);
        } else {
            CalendarFragment calendarFragment = (CalendarFragment) getSupportFragmentManager()
                    .findFragmentByTag(CalendarFragment.class.getName());

            if (calendarFragment != null) {
                if (calendarFragment.closeSearch()) {
                    return;
                }
            }
        }


        if (isDoubleTap()) {
            if (!getSharedPreferences(isCommnetedBefore,
                    Context.MODE_PRIVATE).getBoolean(isCommnetedBefore, false)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage("برای حمایت از ما لطفا نظر خود را ثبت کنید \uD83D\uDE0D");
                dialog.setPositiveButton("باشه", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = getSharedPreferences(isCommnetedBefore,
                                Context.MODE_PRIVATE);
                        sharedPreferences.edit().putBoolean(isCommnetedBefore, true).apply();

                        try {
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setData(Uri.parse("bazaar://details?id=" + getPackageName()));
                intent.setPackage("com.farsitel.bazaar");
                startActivity(intent);

            } catch (Exception e) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://cafebazaar.ir/app/" + getPackageName() + "/?l=fa"));
                startActivity(browserIntent);
            }

                      /* Uri uri = Uri.parse("market://details?id=" + getPackageName());
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                        }*/
                    }
                });
                dialog.setNegativeButton("بعدا..", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.super.onBackPressed();
                    }
                });
                dialog.show();

            }
            else {
                super.onBackPressed();
            }

        } else {
            Toast.makeText(getApplicationContext(), "برای خروج یکبار دیگر کلیک کنید", Toast.LENGTH_SHORT).show();
        }
    }

    String isCommnetedBefore = "isCommnetedBefore";
    long last_clicked_time = 0;
    long max_dif_millis = 2000;

    private boolean isDoubleTap() {
        long current_time = System.currentTimeMillis();
        long dif = current_time - last_clicked_time;
        last_clicked_time = current_time;
        if (dif < max_dif_millis)
            return true;
        else
            return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Checking for the "menu" key
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void restartActivity(int item) {
        Intent intent = getIntent();
        if (item == CONVERTER)
            intent.setAction("CONVERTER_SHORTCUT");
        else if (item == COMPASS)
            intent.setAction("COMPASS_SHORTCUT");
        else if (item == PREFERENCE)
            intent.setAction("PREFERENCE_SHORTCUT");
       /* else if (item == ABOUT)
            intent.setAction("ABOUT_SHORTCUT");
*/
        finish();
        startActivity(intent);
    }

    public void selectItem(int item) {
        if (item == EXIT) {
            finish();
            return;
        }

        if (menuPosition != item) {
            if (settingHasChanged && menuPosition == PREFERENCE) { // update on returning from preferences
                Utils.initUtils(this);
                UpdateUtils.update(getApplicationContext(), true);
            }

            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_holder,
                                (Fragment) fragments[item].newInstance(),
                                fragments[item].getName()
                        ).commit();
                menuPosition = item;
            } catch (Exception e) {
                Log.e(TAG, item + " is selected as an index", e);
            }
        }

        adapter.setSelectedItem(menuPosition);

        drawerLayout.closeDrawers();
    }
}
