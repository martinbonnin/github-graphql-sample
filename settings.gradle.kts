include(":app")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        mavenLocal {
            content {
                includeGroup("com.apollographql.apollo")
                includeGroupByRegex(".*-SNAPSHOT")
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
