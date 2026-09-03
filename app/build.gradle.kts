import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties()

val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val devLoginUrl: String = localProperties.getProperty("LOGIN_URL_DEV")
val devRefreshTokenUrl: String = localProperties.getProperty("REFRESH_TOKEN_URL_DEV")
val devGenealogyDateSettingUrl: String =
    localProperties.getProperty("GENEALOGY_DATE_SETTING_URL_DEV")
val devGenealogyDateUpcomingUrl: String =
    localProperties.getProperty("GENEALOGY_DATE_UPCOMING_URL_DEV")
val devGenealogySiteUrl: String =
    localProperties.getProperty("GENEALOGY_SITE_URL_DEV")
val devGenealogySiteBaseUrl: String =
    localProperties.getProperty("GENEALOGY_SITE_BASE_URL_DEV")
val devGenealogySitePersonUrl: String =
    localProperties.getProperty("GENEALOGY_SITE_PERSON_URL_DEV")
val prodLoginUrl: String = localProperties.getProperty("LOGIN_URL_PROD")
val prodRefreshTokenUrl: String = localProperties.getProperty("REFRESH_TOKEN_URL_PROD")
val prodGenealogyDateSettingUrl: String =
    localProperties.getProperty("GENEALOGY_DATE_SETTING_URL_PROD")
val prodGenealogyDateUpcomingUrl: String =
    localProperties.getProperty("GENEALOGY_DATE_UPCOMING_URL_PROD")
val prodGenealogySiteUrl: String =
    localProperties.getProperty("GENEALOGY_SITE_URL_PROD")
val prodGenealogySiteBaseUrl: String =
    localProperties.getProperty("GENEALOGY_SITE_URL_PROD")
val prodGenealogySitePersonUrl: String =
    localProperties.getProperty("GENEALOGY_SITE_BASE_URL_PROD")
val storePasswordVal: String =
    localProperties.getProperty("STORE_PASSWORD")
val keyAliasVal: String =
    localProperties.getProperty("KEY_ALIAS")
val keyPasswordVal: String =
    localProperties.getProperty("KEY_PASSWORD")

android {
    namespace = "net.danshin.danshinnoticeandroidphone"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "net.danshin.danshinnoticeandroidphone"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../key.jks")
            storePassword = storePasswordVal
            keyAlias = keyAliasVal
            keyPassword = keyPasswordVal
        }
    }

    buildFeatures {
        resValues = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "login_url", prodLoginUrl)
            resValue("string", "refresh_token_url", prodRefreshTokenUrl)
            resValue("string", "genealogy_date_setting_url", prodGenealogyDateSettingUrl)
            resValue("string", "genealogy_date_upcoming_url", prodGenealogyDateUpcomingUrl)
            resValue("string", "genealogy_site_url", prodGenealogySiteUrl)
            resValue("string", "genealogy_site_base_url", prodGenealogySiteBaseUrl)
            resValue("string", "genealogy_site_person_url", prodGenealogySitePersonUrl)
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            resValue("bool", "is_debug", "false")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            resValue("string", "login_url", devLoginUrl)
            resValue("string", "refresh_token_url", devRefreshTokenUrl)
            resValue("string", "genealogy_date_setting_url", devGenealogyDateSettingUrl)
            resValue("string", "genealogy_date_upcoming_url", devGenealogyDateUpcomingUrl)
            resValue("string", "genealogy_site_url", devGenealogySiteUrl)
            resValue("string", "genealogy_site_base_url", devGenealogySiteBaseUrl)
            resValue("string", "genealogy_site_person_url", devGenealogySitePersonUrl)
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            resValue("bool", "is_debug", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.splashscreen)
    implementation(libs.concurrent.futures)
    implementation(libs.rules)
    implementation(libs.lifecycle.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}