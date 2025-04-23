plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Google services plugin
    alias(libs.plugins.kotlin.android) // Kotlin plugin
    id ("kotlin-parcelize")

}

android {
    namespace = "com.example.yeniproje"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.yeniproje"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database) // Firebase Database
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.runtime.lint)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage) // Firebase Auth
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.squareup.picasso:picasso:2.8")
// Glide
    implementation ("com.github.bumptech.glide:glide:4.13.2")

// Picasso
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.google.firebase:firebase-auth:21.0.3")

    // Firebase BOM (Bill of Materials) for consistent version management
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Authentication and Google Play services
    implementation("com.google.firebase:firebase-auth:21.0.8")
    implementation ("com.google.firebase:firebase-database:20.3.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    // OkHttp kütüphanesi - HTTP istekleri için kullanılır
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")

    // JSON işleme kütüphanesi - JSON verisi ile çalışmak için
    implementation ("org.json:json:20210307")
    // Picasso kütüphanesi - Resimleri yüklemek için kullanılır
    implementation ("com.squareup.picasso:picasso:2.71828")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation ("com.ticketmaster.presence:secure-entry:1.2.9")



}
