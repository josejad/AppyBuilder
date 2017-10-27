Hossein,
This version of google-play-services.jar was copied from location below[1]. However, the filesize is 2,700kb.

The dx.jar in DexExecTask.java had problem during building and was not able to properly dex the above jar classes. As result, it seemed like dex.jar was old version. I copied a later version of the dx.jar from [2] and copied to [3]. Apparently the dx.jar in [3] was an older version.
Next, tried the build again, then got error messages about --force-jumbo. Googling showed that I had to add --force-jumbo when DEXing; so I added that in the java code.
Tried rebuild again, and got message about too many strings. Apparently the 2.7mb is large and dex can't handle. So I opened the google-play-services and deleted the maps and analytic packages that I don't need for AdMob. That worked with no problem!

[1]
C:\tools\android\extras\google\google_play_services\libproject\google-play-services_lib\libs

[2]
C:\tools\android\build-tools\19.1.0\lib

[3]
C:\projects2\ai2SandboxV9\app-inventor-private\appinventor\lib\android\tools

References:
http://stackoverflow.com/questions/23527218/application-too-big-unable-to-execute-dex-cannot-merge-new-index-into-a-non-ju
http://stackoverflow.com/questions/8487268/android-dx-tool