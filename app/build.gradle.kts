plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.medicinereminderapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.medicinereminderapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation ("com.google.guava:guava:29.0-android")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.1.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0-alpha01")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("com.firebaseui:firebase-ui-database:8.0.2")
    implementation ("com.orhanobut:dialogplus:1.11@aar")
    implementation ("androidx.work:work-runtime:2.7.1")
    implementation ("com.google.firebase:firebase-auth:21.0.5")
    implementation ("com.google.firebase:firebase-database:20.0.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}