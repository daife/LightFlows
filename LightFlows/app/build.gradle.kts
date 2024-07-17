
plugins {
    id("com.android.application")
    
}

android {
    namespace = "com.dypho.lightflows"
    compileSdk = 33
    
    defaultConfig {
        applicationId = "com.dypho.lightflows"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        
    }
    
}

dependencies {

implementation(files("Msc.jar"))
implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.9.0")
  implementation("org.bytedeco:javacv:1.5.9")
    implementation("org.bytedeco:javacpp-presets:1.5.9")
    implementation("org.bytedeco:openblas:0.3.23-1.5.9:android-arm")//这两行一起编译似乎会报错，可以注释其中一行，分别编译打包，然后将lib整合到一块
    implementation("org.bytedeco:opencv:4.7.0-1.5.9:android-arm")//
    implementation("com.squareup.okhttp3:okhttp:4.2.2")
}
