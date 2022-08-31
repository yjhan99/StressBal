pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {  // Only for snapshot artifacts
            name = "ossrh-snapshot"
            url  = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")