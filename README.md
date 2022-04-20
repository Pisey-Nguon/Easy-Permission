
# Easy-Permission
[![](https://jitpack.io/v/Pisey-Nguon/Easy-Permission.svg)](https://jitpack.io/#Pisey-Nguon/Easy-Permission)

![20220420_144106](https://user-images.githubusercontent.com/47247206/164179342-0251c037-ce07-44ca-80bb-41b04706d361.gif)


How to
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

*If your Gradle version below 7.0*
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' } //add this line
		}
	}

*if your Gradle version from 7.0*
Add it in your root settings.gradle at the end of repositories:

    dependencyResolutionManagement {  
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  
        repositories {
	        google()  
            mavenCentral()  
            jcenter()  
            maven { url 'https://jitpack.io' }  //add this line
     }}

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.Pisey-Nguon:Easy-Permission:1.0.0'
	}

Step 3. Please make sure you add any permission that need to manifest, Example:

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.CAMERA" />

Step 4. Finally you can request permission by

    runWithPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA,){  
      Toast.makeText(this, "Permission Allowed", Toast.LENGTH_SHORT).show()  
    }
