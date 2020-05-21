plugins {
    id("com.android.application").version("4.1.0-alpha09")
    id("org.jetbrains.kotlin.android").version("1.3.72")
    id("org.jetbrains.kotlin.android.extensions").version("1.3.72")
    id("com.apollographql.apollo").version("2.0.3")
}

apply(from = "github.properties")

fun githubToken(): String {
    return if (extra.has("githubToken")) {
        extra.get("githubToken") as String
    } else {
        "set me in github.properties"
    }
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "net.mbonnin.githubgraphqlsample"
        minSdkVersion(21)
        versionCode = 1
        versionName = "1"

        val token = githubToken()

        buildConfigField("String", "GITHUB_TOKEN", "\"$token\"")
    }

    signingConfigs {
        create("dummy") {
            keyAlias = "dummy"
            keyPassword = "dummy0"
            storeFile = project.file( "keystore.jks")
            storePassword = "dummy0"
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("dummy")
        }
    }

    buildTypes {
        create("staging") {
            initWith(getByName("debug"))
        }
        create("prodEnv") {
            initWith(getByName("debug"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    val apolloVersion = "2.0.3"
    add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    add("implementation", "com.android.support:appcompat-v7:28.0.0")
    add("implementation", "com.android.support:recyclerview-v7:28.0.0")
    add("implementation", "com.android.support.constraint:constraint-layout:1.1.3")
    add("testImplementation", "junit:junit:4.12")
    add("androidTestImplementation", "com.android.support.test:runner:1.0.2")
    add("androidTestImplementation", "com.android.support.test.espresso:espresso-core:3.0.2")
    add("implementation", "com.apollographql.apollo:apollo-runtime:$apolloVersion")
    add("implementation", "com.apollographql.apollo:apollo-rx2-support:$apolloVersion")
    add("implementation", "com.apollographql.apollo:apollo-coroutines-support:$apolloVersion")
    add("implementation", "com.apollographql.apollo:apollo-normalized-cache-sqlite:$apolloVersion")
    add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
    add("implementation", "com.jakewharton.timber:timber:4.7.1")
    add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    add("implementation", "io.reactivex.rxjava2:rxandroid:2.1.0")
}
androidExtensions {
    isExperimental = true
}
apollo {
    generateKotlinModels.set(true)
    customTypeMapping.set(mutableMapOf("DateTime" to "String"))
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.register("downloadSchema", com.apollographql.apollo.gradle.internal.ApolloDownloadSchemaTask::class.java) {
    endpointUrl.set("https://api.github.com/graphql")
    schemaFilePath.set("src/main/graphql/net/mbonnin/githubgraphqlsample/schema.json")
    headers.set(mapOf("Authorization" to "Bearer ${githubToken()}"))
}