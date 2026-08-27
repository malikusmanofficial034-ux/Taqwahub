import java.util.Base64
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "xfuture.studio.taqwahub.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 3
    versionName = "1.0.2"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePropsFile = rootProject.file("keystore.properties")
      val keystoreProps = Properties()
      if (keystorePropsFile.exists()) {
        val stream = keystorePropsFile.inputStream()
        keystoreProps.load(stream)
        stream.close()
      }

      val envStoreFile = System.getenv("RELEASE_STORE_FILE") ?: keystoreProps.getProperty("storeFile")
      storeFile = if (!envStoreFile.isNullOrEmpty()) {
        file(envStoreFile)
      } else {
        file("${rootDir}/my-upload-key.jks")
      }

      storePassword = System.getenv("RELEASE_STORE_PASSWORD")
        ?: keystoreProps.getProperty("storePassword")
        ?: ""

      keyAlias = System.getenv("RELEASE_KEY_ALIAS")
        ?: keystoreProps.getProperty("keyAlias")
        ?: ""

      keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        ?: keystoreProps.getProperty("keyPassword")
        ?: ""
    }
    getByName("debug") {
      // Decode base64 debug keystore if present
      val base64File = file("${rootDir}/debug.keystore.base64")
      val destKeystore = file("${projectDir}/debug.keystore")
      if (base64File.exists()) {
        try {
          val base64Str = base64File.readText().trim()
          val decoded = Base64.getDecoder().decode(base64Str)
          destKeystore.writeBytes(decoded)
          println("Unconditionally decoded debug.keystore.base64 to ${destKeystore.absolutePath}")
        } catch (e: Exception) {
          println("Failed to decode debug.keystore.base64: ${e.message}")
        }
      }
      if (destKeystore.exists()) {
        storeFile = destKeystore
        if (base64File.exists()) {
          storePassword = "android"
          keyAlias = "androiddebugkey"
          keyPassword = "android"
        } else {
          storePassword = "android"
          keyAlias = "androiddebugkey"
          keyPassword = "android"
        }
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      isShrinkResources = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      
      // Fallback to debug signing if release keystore is missing, preserving existing setup
      val keystorePropsFile = rootProject.file("keystore.properties")
      val keystoreProps = Properties()
      if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { keystoreProps.load(it) }
      }
      val envStoreFile = System.getenv("RELEASE_STORE_FILE") ?: keystoreProps.getProperty("storeFile")
      val hasKeystore = if (!envStoreFile.isNullOrEmpty()) {
        file(envStoreFile).exists()
      } else {
        file("${rootDir}/my-upload-key.jks").exists()
      }
      signingConfig = if (hasKeystore) {
        signingConfigs.getByName("release")
      } else {
        signingConfigs.getByName("debug")
      }
    }
    debug {
      signingConfig = signingConfigs.getByName("debug")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}



tasks.register("generateKeystore", Exec::class) {
    val file = file("${rootDir}/my-upload-key.jks")
    if (file.exists()) {
        file.delete()
    }
    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties()
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { keystoreProps.load(it) }
    }
    val pass = System.getenv("RELEASE_STORE_PASSWORD")
        ?: keystoreProps.getProperty("storePassword")
        ?: ""
    val dname = System.getenv("RELEASE_DNAME") ?: "CN=Taqwahub, OU=Taqwahub Llc, O=Taqwahub Llc, C=PK"
    val alias = System.getenv("RELEASE_KEY_ALIAS")
        ?: keystoreProps.getProperty("keyAlias")
        ?: "upload"

    doFirst {
        if (pass.isEmpty()) {
            throw GradleException("RELEASE_STORE_PASSWORD or storePassword property is required to generate a keystore.")
        }
    }

    commandLine(
        "keytool",
        "-genkeypair",
        "-v",
        "-keystore",
        "${rootDir}/my-upload-key.jks",
        "-alias",
        alias,
        "-keyalg",
        "RSA",
        "-keysize",
        "2048",
        "-validity",
        "10000",
        "-dname",
        dname,
        "-storepass",
        pass,
        "-keypass",
        pass
    )
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation("com.google.firebase:firebase-auth")
  implementation("com.google.firebase:firebase-firestore")
  implementation("com.google.firebase:firebase-messaging")
  implementation(libs.androidx.work.runtime.ktx)
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.security.crypto)
  implementation("androidx.glance:glance-appwidget:1.1.0")
  implementation("androidx.glance:glance-material3:1.1.0")
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.androidx.media3.exoplayer)
  // implementation(libs.androidx.media3.ui)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.play.services.ads)
  implementation(libs.play.review)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

