package net.danshin.danshinnoticeandroidphone.config;

import android.content.Context;

import net.danshin.danshinnoticeandroidphone.R;

public class BuildConfig {
    private static String loginUrl;
    private static String refreshTokenUrl;
    private static String genealogyDateSettingUrl;
    private static String genealogyDateUpcomingUrl;
    private static String genealogySiteUrl;
    private static String genealogySiteBaseUrl;
    private static String genealogySitePersonUrl;
    private static boolean debug;

    private BuildConfig() {
    }

    public static void init(Context context) {
        loginUrl = context.getString(R.string.login_url);
        refreshTokenUrl = context.getString(R.string.refresh_token_url);
        genealogyDateSettingUrl = context.getString(R.string.genealogy_date_setting_url);
        genealogyDateUpcomingUrl = context.getString(R.string.genealogy_date_upcoming_url);
        genealogySiteUrl = context.getString(R.string.genealogy_site_url);
        genealogySiteBaseUrl = context.getString(R.string.genealogy_site_base_url);
        genealogySitePersonUrl = context.getString(R.string.genealogy_site_person_url);
        debug = context.getResources().getBoolean(R.bool.is_debug);
    }

    public static boolean isDebug() {
        return debug;
    }

    public static String getGenealogyDateUpcomingUrl() {
        return genealogyDateUpcomingUrl;
    }

    public static String getGenealogySiteUrl() {
        return genealogySiteUrl;
    }

    public static String getGenealogyDateSettingUrl() {
        return genealogyDateSettingUrl;
    }

    public static String getLoginUrl() {
        return loginUrl;
    }

    public static String getGenealogySiteBaseUrl() {
        return genealogySiteBaseUrl;
    }

    public static String getRefreshTokenUrl() {
        return refreshTokenUrl;
    }

    public static String getGenealogySitePersonUrl() {
        return genealogySitePersonUrl;
    }
}
