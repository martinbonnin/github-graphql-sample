// Top-level build file where you can add configuration options common to all sub-projects/modules.

allprojects {
    repositories {
        mavenCentral()
        google()
        jcenter()
        mavenLocal {
            content {
                includeVersionByRegex("com.apollographql.apollo", ".*", ".*-SNAPSHOT")
            }
        }
    }
}

