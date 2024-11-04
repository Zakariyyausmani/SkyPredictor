plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wavesoffood"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wavesoffood"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
buildscript {
    dependencies {
        // Add this line
        classpath ("com.google.gms:google-services:4.3.15") // Check for the latest version
    }
}
dependencies {
    // Other dependencies
    implementation ("com.google.android.gms:play-services-location:21.0.1")
}

dependencies {
    implementation ("com.google.android.material:material:1.6.1")
    implementation ("androidx.appcompat:appcompat:1.6.0")
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("com.google.code.gson:gson:2.10.1") // Gson dependency
    // Kotlin stdlib
    // Other dependencies...
}
dependencies {
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")// Check for the latest version
    implementation ("com.google.android.gms:play-services-location:19.0.1") // Check for the latest version
}




dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.9.1")
    implementation("com.google.firebase:firebase-database:20.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.airbnb.android:lottie:3.4.0")
}