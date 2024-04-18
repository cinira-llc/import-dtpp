import com.mooltiverse.oss.nyx.gradle.NyxExtension;

plugins {
    id("com.mooltiverse.oss.nyx") version "3.0.1"
}

extensions.getByName<NyxExtension>("nyx").apply {
    initialVersion = "1.0.0"
    preset = "simple"
    verbosity = "INFO"
    releaseTypes {
        publicationServices.add("github")
        items {
            register("mainline") {
                gitCommit = "true"
            }
        }
    }
    services {
        register("github") {
            type = "GITHUB"
            options {
                "AUTHENTICATION_TOKEN" to "{{#environmentVariable}}GH_TOKEN{{/environmentVariable}}"
                "REPOSITORY_NAME" to "artifacts"
                "REPOSITORY_OWNER" to "cinira-llc"
            }
        }
    }
}
