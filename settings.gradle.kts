pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PhoneBackupPro"

// Include all modules
include(":app")
include(":core")
include(":features:backup")
include(":features:restore")
include(":features:transfer")
include(":features:whatsapp")
include(":features:cloud")
