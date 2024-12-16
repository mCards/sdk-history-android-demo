# mCards Android History SDK Demo App

The mCards android History SDK encapsulates the following functionality:

1. View user's history list
2. View details of an individual history item 
3. Transaction tracking reports 
4. Change funding account associated with history item

# Usage
Implementing apps MUST override this string value for auth0 to work:

<string name="auth0_domain">your value here</string>

Theis value is gotten from the mCards team after setting up the client's auth0 instance.

You must then also update the manifest placeholders in the build.gradle file:

e.g. addManifestPlaceholders(mapOf("auth0Domain" to "@string/auth0_domain", "auth0Scheme" to "your app ID"))


# Importing the Auth SDK
Add the following to your module-level build.gradle:

Groovy:
```
implementation "com.mcards.sdk:history:$latestVersion"
```

Kotlin:
```
implementation("com.mcards.sdk:history:$latestVersion")
```

And the following to the project settings.gradle:
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://maven.pkg.github.com/Wantsa/sdk-history-android")
            credentials {
                username = GITHUB_USERNAME
                password = GITHUB_TOKEN
            }
        }
    }
}
```

# Documentation
\\\\\Add documentation links here/////
