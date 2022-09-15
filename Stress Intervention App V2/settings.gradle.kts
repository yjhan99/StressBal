pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {         // add this repo to use snapshots
            name = "ossrh-snapshot"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {         // add this repo to use snapshots
            name = "ossrh-snapshot"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

include(":app")
include(":transfer_api")