Wildfire is an anonymous messaging app running on Android 4.0+ platform. It uses the adaptive diffusion spreading mechanism from (Fanti et al., Sigmetrics 2015) to provide users with strong anonymity guarantees. Currently an alpha version of Wildfire is available. 

Disclaimer: This version of Wildfire does NOT currently provide full anonymity guarantees, and is intended as a proof-of-concept. It is run over Antox, a distributed, peer-to-peer messaging platform (www.github.com/astonex/Antox). This platform uses a DHT for namespace resolution, and we have no guarantees that this DHT is not compromised or monitored. In practice, this could compromise the anonymity guarantees provided by adaptive diffusion.  

Current functions:
— Anonymous posts
— Optional message forwarding
— Adaptive transmission

Getting Wildfire
— Available at Github repository: https://github.com/gfanti/Wildfire

App installation
— Copy app-release.apk (/Wildfire/app) to your phone
— Double click it, and install it

Compiling instructions for developer:
— Enter Tools > Android > SDK Manager, and make sure that your apk level is above 21
— Install Scala plugin in Android Studio and restart it
— Download source code from: https://github.com/gfanti/Wildfire
— Open the project in Android Studio
— Enter File > Import Existing Project, and then select build.gradle file in the root of Wildfire folder
— Download the lastest tox4j binaries (this can be done by running download-depedencies.sh / download-dependecies,bat)
— Build the project and run it. You can test the app on an emulator, or on you real phone through developer mode
