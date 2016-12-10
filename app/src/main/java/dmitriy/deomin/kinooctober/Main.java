package dmitriy.deomin.kinooctober;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import dmitriy.deomin.kinooctober.Info.Pravila_kinoteatra;
import dmitriy.deomin.kinooctober.Info.Vopr;
import dmitriy.deomin.kinooctober.User_kabinet.User_login_parol;
import dmitriy.deomin.kinooctober.kino_sourse.Mani;
import dmitriy.deomin.kinooctober.kino_sourse.News;
import dmitriy.deomin.kinooctober.kino_sourse.Otzivi;
import dmitriy.deomin.kinooctober.kino_sourse.Scoro;
import dmitriy.deomin.kinooctober.kino_sourse.Sessions;
import dmitriy.deomin.kinooctober.libries.RippleView;


public class Main extends FragmentActivity implements View.OnClickListener {

    static ViewPager viewPager;
    static Myadapter myadapter;

    public static int width_d;
    public static int heigh_d;

    Button mani;
    Button skoro;
    Button otzivi;
    Button session;
    Button news;
    Button vopros;
    public static Context context;

    static public Typeface face;

    public static int COLOR_FON;
    public static int COLOR_ITEM;
    public static int COLOR_ITEM2;
    public static int COLOR_TEXT;
    static public int TIME_SHOW_REKLAMA; // сколько показывать рекламу
    boolean time_show_reklamma; //
    boolean visi;//true при активном приложении

    public static LinearLayout liner_boss;

    public static SharedPreferences mSettings; // сохранялка
    public static final String APP_PREFERENCES = "mysettings"; // файл сохранялки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = getApplicationContext();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);


        face = Typeface.createFromAsset(getAssets(), ((save_read("fonts").equals("")) ? "fonts/Tweed.ttf" : save_read("fonts")));

        //ставим цвет фона
        if (save_read_int("color_fon") == 0) {
            COLOR_FON = getResources().getColor(R.color.fon);
        } else {
            COLOR_FON = save_read_int("color_fon");
        }
        //ставим цвет постов
        if (save_read_int("color_post1") == 0) {
            COLOR_ITEM = getResources().getColor(R.color.post1);
        } else {
            COLOR_ITEM = save_read_int("color_post1");
        }
        //ставим цвет постов
        if (save_read_int("color_post2") == 0) {
            COLOR_ITEM2 = getResources().getColor(R.color.post2);
        } else {
            COLOR_ITEM2 = save_read_int("color_post2");
        }

        //ставим цвеи текста
        if (save_read_int("color_text") == 0) {
            COLOR_TEXT = getResources().getColor(R.color.text);
        } else {
            COLOR_TEXT = save_read_int("color_text");
        }



        //во весь экран
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        TIME_SHOW_REKLAMA = 10; //секнды показа рекламы
        visi = true;  // приложение активно

        liner_boss = (LinearLayout) findViewById(R.id.liner_boss);
        liner_boss.setBackgroundColor(COLOR_FON);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        width_d = display.getWidth();
        heigh_d = display.getHeight();

        if (Main.save_read("ima_user").length() < 2) {
            Main.save_value("ima_user", getUniqueID());
        }

        //если id нет  значит и авторизации нет
        if (save_read("id_user").length() < 1) {
            save_value_bool("authenticate", false);
        }

        myadapter = new Myadapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(6);
        viewPager.setAdapter(myadapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                // номер страницы
                fon_button(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        final RippleView rippleView = (RippleView) findViewById(R.id.logo_riple);
        rippleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Open_menu();
                    }
                }, 800);
            }
        });

        news = ((Button) findViewById(R.id.button_news));
        session = ((Button) findViewById(R.id.button_sessions));
        skoro = ((Button) findViewById(R.id.button_skoro));
        otzivi = ((Button) findViewById(R.id.button_otzivi));
        mani = ((Button) findViewById(R.id.button_mani));
        vopros = ((Button) findViewById(R.id.button_vopros));

        news.setTypeface(face);
        session.setTypeface(face);
        skoro.setTypeface(face);
        otzivi.setTypeface(face);
        mani.setTypeface(face);
        vopros.setTypeface(face);

        news.setTextColor(COLOR_TEXT);
        session.setTextColor(COLOR_TEXT);
        skoro.setTextColor(COLOR_TEXT);
        otzivi.setTextColor(COLOR_TEXT);
        mani.setTextColor(COLOR_TEXT);
        vopros.setTextColor(COLOR_TEXT);

        news.setOnClickListener(this);
        session.setOnClickListener(this);
        skoro.setOnClickListener(this);
        otzivi.setOnClickListener(this);
        mani.setOnClickListener(this);
        vopros.setOnClickListener(this);

        //открываем по умочанию расписание
        viewPager.setCurrentItem(1);


        //подсказки

        final ToolTipRelativeLayout toolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.tol_menu);

        if(save_read_int("start_menu")==0){

            ToolTipView myToolTipView;

            ToolTip toolTip = new ToolTip()
                    .withText("Клик по логотипу откроет меню")
                    .withColor(COLOR_ITEM2)
                    .withTextColor(COLOR_TEXT)
                    .withShadow()
                    .withAnimationType(ToolTip.AnimationType.FROM_TOP);
            myToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.logo_riple));
            myToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
                @Override
                public void onToolTipViewClicked(ToolTipView toolTipView) {
                    toolTipRelativeLayout.setVisibility(View.GONE);
                    save_value_int("start_menu",1);
                }
            });
        }else {
            toolTipRelativeLayout.setVisibility(View.GONE);
        }





        //реклама
        final AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        time_show_reklamma = false;  //если бы черти isVisible mAdView сделали это херня бы не пригодилась

        //если нет интеренета скроем еЁ
        if (!isNetworkConnected()) {
            mAdView.setVisibility(View.GONE);
        } else {
            //через 10 секунд скроем её(пока так потом можно регулировать от количества постов)
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.v("TTT","ebasit");
                    if (visi) {
                        if (time_show_reklamma) {
                            mAdView.setVisibility(View.GONE); // скроем рекламу и поток больше не запустится
                        } else {
                            //иначе покажем
                            mAdView.setVisibility(View.VISIBLE);
                            time_show_reklamma = true; // это нужно чтоб знать что реклама показна
                            handler.postDelayed(this, 1000 * TIME_SHOW_REKLAMA); // через 10 секунд вырубим рекламу
                        }
                    }else {
                        handler.postDelayed(this, 1000 * 2); // если приложение свернуто пока в пустую погоняем поток
                    }
                }
            });
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            event.startTracking();
            Open_menu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public class Myadapter extends FragmentPagerAdapter {
        public Myadapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new News();
                case 1:
                    return new Sessions();
                case 2:
                    return new Scoro();
                case 3:
                    return new Otzivi();
                case 4:
                    return new Vopr();
                case 5:
                    return new Mani();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 6;
        }
    }


    public void Open_menu() { // на кнопку меню
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo));
        final View content = LayoutInflater.from(Main.this).inflate(R.layout.menu_progi,null);
        builder.setView(content);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        ((LinearLayout) content.findViewById(R.id.menu_progi_alert)).setBackgroundColor(COLOR_FON);
        ((Button) content.findViewById(R.id.button_setting)).setTypeface(face);
        ((Button) content.findViewById(R.id.button_user_kabinet)).setTypeface(face);
        ((Button) content.findViewById(R.id.button_pozvonit)).setTypeface(face);
        ((Button) content.findViewById(R.id.button_abaut)).setTypeface(face);
        ((Button) content.findViewById(R.id.button_pravila)).setTypeface(face);


        ((Button) content.findViewById(R.id.button_pravila)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);
                Intent p = new Intent(Main.this, Pravila_kinoteatra.class);
                startActivity(p);
            }
        });


        ((Button) content.findViewById(R.id.button_setting)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);
                Intent s = new Intent(Main.this, Setting.class);
                startActivity(s);
            }
        });
        ((Button) content.findViewById(R.id.button_user_kabinet)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);
                Intent l = new Intent(Main.this, User_login_parol.class);
                startActivity(l);
            }
        });
        ((Button) content.findViewById(R.id.button_pozvonit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);
                dual_vopros();
            }
        });
        ((Button) content.findViewById(R.id.button_abaut)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);
                Intent i = new Intent(Main.this, Abaut.class);
                startActivity(i);
            }
        });
    }

    public void fon_button(int b) {
        switch (b) {
            case (0): // R.id.button_news
                news.setBackgroundColor(COLOR_ITEM);

                vopros.setBackgroundColor(COLOR_FON);
                session.setBackgroundColor(COLOR_FON);
                skoro.setBackgroundColor(COLOR_FON);
                otzivi.setBackgroundColor(COLOR_FON);
                mani.setBackgroundColor(COLOR_FON);
                break;
            case (1): //R.id.button_sessions
                session.setBackgroundColor(COLOR_ITEM);

                vopros.setBackgroundColor(COLOR_FON);
                news.setBackgroundColor(COLOR_FON);
                skoro.setBackgroundColor(COLOR_FON);
                otzivi.setBackgroundColor(COLOR_FON);
                mani.setBackgroundColor(COLOR_FON);
                break;
            case (2):// R.id.button_skoro
                skoro.setBackgroundColor(COLOR_ITEM);

                vopros.setBackgroundColor(COLOR_FON);
                news.setBackgroundColor(COLOR_FON);
                session.setBackgroundColor(COLOR_FON);
                otzivi.setBackgroundColor(COLOR_FON);
                mani.setBackgroundColor(COLOR_FON);
                break;
            case (3)://R.id.button_otzivi
                otzivi.setBackgroundColor(COLOR_ITEM);

                vopros.setBackgroundColor(COLOR_FON);
                news.setBackgroundColor(COLOR_FON);
                session.setBackgroundColor(COLOR_FON);
                skoro.setBackgroundColor(COLOR_FON);
                mani.setBackgroundColor(COLOR_FON);
                break;
            case (5)://R.id.button_mani
                mani.setBackgroundColor(COLOR_ITEM);

                vopros.setBackgroundColor(COLOR_FON);
                news.setBackgroundColor(COLOR_FON);
                session.setBackgroundColor(COLOR_FON);
                skoro.setBackgroundColor(COLOR_FON);
                otzivi.setBackgroundColor(COLOR_FON);
                break;
            case (4)://R.id.button_vopros
                vopros.setBackgroundColor(COLOR_ITEM);

                news.setBackgroundColor(COLOR_FON);
                session.setBackgroundColor(COLOR_FON);
                skoro.setBackgroundColor(COLOR_FON);
                otzivi.setBackgroundColor(COLOR_FON);
                mani.setBackgroundColor(COLOR_FON);
                break;
        }
    }

    public void dual_vopros() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(Main.this, android.R.style.Theme_Holo));
        final View content = LayoutInflater.from(Main.this).inflate(R.layout.vopros_zvonit_ili_net, null);
        builder.setView(content);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        ((LinearLayout) content.findViewById(R.id.fon_dialoga_pri_zvonke)).setBackgroundColor(COLOR_FON);
        ((Button) content.findViewById(R.id.zvonit_net)).setTypeface(face);
        ((Button) content.findViewById(R.id.zvonit_da)).setTypeface(face);


        ((Button) content.findViewById(R.id.zvonit_net)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);
                alertDialog.dismiss();
            }
        });


        ((Button) content.findViewById(R.id.zvonit_da)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.myalpha);
                v.startAnimation(anim);

                alertDialog.dismiss();
                // звоним
                Intent i = new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:" + "+74754552191"));
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(i);
            }
        });

    } // вопрос и звонок

    //*******************************************************
    public static void save_value_bool(String Key, boolean Value) { //сохранение строки
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Key, Value);
        editor.apply();
    }

    public static boolean save_read_bool(String key_save) {  // чтение настройки
        if (mSettings.contains(key_save)) {
            return (mSettings.getBoolean(key_save, false));
        } else {
            return false;
        }
    }

    public static void save_value(String Key, String Value) { //сохранение строки
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(Key, Value);
        editor.apply();
    }

    public static String save_read(String key_save) {  // чтение настройки
        if (mSettings.contains(key_save)) {
            return (mSettings.getString(key_save, ""));
        }
        return "";
    }

    public static void save_value_int(String Key, int Value) { //сохранение строки
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(Key, Value);
        editor.apply();
    }

    public static int save_read_int(String key_save) {  // чтение настройки
        if (mSettings.contains(key_save)) {
            return (mSettings.getInt(key_save, 0));
        }
        return 0;
    }

    //запись
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    public static void putText(String text) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
            Toast("Скопировали в буфер: " + text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = ClipData.newPlainText(text, text);
            clipboard.setPrimaryClip(clip);
            Toast("Скопировали в буфер: " + text);
        }
    }

    //чтение
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    public String getText() {
        String text = null;
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.getText() != null) {
                text = clipboard.getText().toString();
            }
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.getText() != null) {
                text = clipboard.getText().toString();
            }
        }
        return text;
    }

    public static boolean setStringArrayPref(Context context, String key, ArrayList<String> values) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        return editor.commit();
    }

    public static ArrayList<String> getStringArrayPref(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList<String> urls = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }
    //*************************************************************

    public static void open_session() {


        myadapter.notifyDataSetChanged();
        viewPager.setAdapter(myadapter);
        viewPager.setCurrentItem(1);
    }

    public static void open_skoro() {
        myadapter.notifyDataSetChanged();
        viewPager.setAdapter(myadapter);
        viewPager.setCurrentItem(2);
    }

    public static void open_news() {
        myadapter.notifyDataSetChanged();
        viewPager.setAdapter(myadapter);
        viewPager.setCurrentItem(0);
    }

    public static void open_otzivi() {
        myadapter.notifyDataSetChanged();
        viewPager.setAdapter(myadapter);
        viewPager.setCurrentItem(3);
    }

    public static void open_vopr() {
        myadapter.notifyDataSetChanged();
        viewPager.setAdapter(myadapter);
        viewPager.setCurrentItem(4);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_news:
                viewPager.setCurrentItem(0);
                break;
            case R.id.button_sessions:
                viewPager.setCurrentItem(1);
                break;
            case R.id.button_skoro:
                viewPager.setCurrentItem(2);
                break;
            case R.id.button_otzivi:
                viewPager.setCurrentItem(3);
                break;
            case R.id.button_mani:
                viewPager.setCurrentItem(5);
                break;
            case R.id.button_vopros:
                viewPager.setCurrentItem(4);
                break;
        }
    }

    //****************************************************************************************************************************
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public String getUniqueID(){
        String myAndroidDeviceId = "";
        myAndroidDeviceId =  Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        //возьмём часть
        myAndroidDeviceId = myAndroidDeviceId.substring(0,myAndroidDeviceId.length()/3);
        return myAndroidDeviceId;
    }

    public static void  Toast(String mesag){
        SuperToast.create(context, mesag, SuperToast.Duration.LONG,
                Style.getStyle(Style.GREEN, SuperToast.Animations.FLYIN)).show();
    }
    public static void Toast_error(String mesag){
        SuperToast.create(context, mesag, SuperToast.Duration.LONG,
                Style.getStyle(Style.RED, SuperToast.Animations.POPUP)).show();
    }



    @Override
    protected void onPause() {
        super.onPause();
        visi = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        visi = true;
    }

}
