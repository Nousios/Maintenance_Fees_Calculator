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
    versionCatalogs {
        create("vcs") {
            from(files("gradle/vcs.versions.toml"))
        }
    }
}

rootProject.name = "Maintenance Fees Calculator"
include(":app")
