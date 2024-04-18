import com.mooltiverse.oss.nyx.gradle.NyxExtension;

plugins {
    id("com.mooltiverse.oss.nyx") version "3.0.1"
}

extensions.getByName<NyxExtension>("nyx").apply {
    initialVersion = "1.0.0"
    preset = "simple"
    verbosity = "DEBUG"
    releaseTypes {
        publicationServices.add("github")
        items {
            create("mainline") {
                gitCommit = "true"
            }
        }
    }
    services {
        create("github") {
            type = "GITHUB"
            options {
                "AUTHENTICATION_TOKEN" to "{{#environmentVariable}}GH_TOKEN{{/environmentVariable}}"
                "REPOSITORY_NAME" to "artifacts"
                "REPOSITORY_OWNER" to "cinira-llc"
            }
        }
    }
}
