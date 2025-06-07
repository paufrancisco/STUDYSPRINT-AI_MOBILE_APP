plugins{
    alias(libs.plugins.androidApplication)

    id("com.google.gms.google-services")


}

android {
    namespace = "com.jtdev.umak"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.jtdev.umak"
        minSdk = 26
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


}


dependencies {
    implementation("com.google.android.gms:play-services-auth:20.4.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // MPAndroidChart for charts
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // Firebase BoM to manage versions
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))

    // Firebase libraries, version is managed by BoM
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")





}

