include(":app")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        mavenLocal {
            content {
                includeVersionByRegex("com.apollographql.apollo", ".*", ".*-SNAPSHOT")
            }
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application"){
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}
