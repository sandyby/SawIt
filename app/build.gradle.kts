plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.devtools.ksp")
}

android {
    viewBinding {
        enable = true
    }
    namespace = "com.example.sawit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sawit"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    secrets {
        propertiesFileName = "secrets.properties"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(libs.play.services.location)
    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:${room_version}")
    implementation(libs.play.services.maps)
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui:1.9.3")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.4")
    implementation(libs.androidx.runtime.livedata)
    implementation("com.github.bumptech.glide:glide:5.0.5")
    ksp("com.github.bumptech.glide:compiler:5.0.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.vipulasri:timelineview:1.2.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.5")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.gms:google-services:4.4.4")
    implementation("nl.joery.animatedbottombar:library:1.1.0"){
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("io.github.ehsannarmani:compose-charts:0.2.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    debugImplementation(libs.androidx.ui.tooling)
}