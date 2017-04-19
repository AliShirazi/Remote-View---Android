

innerID SDK Remote View-

A version of our 'experience' applications for the innerID SDK that would use the camera to take a picture of your finger and send it to an AWS EC2 server for processing using Google Protocol Buffer. This processing would involve using a patented algorithm to analyze the image and then convert it to use for authentic fingerprint authentication.  It was developed in 2014 without UI/UX guidance. 

The application uses the innerID SDK, which used Android NDK to pull the C fingerprint processing algorithm and use it in the Android application. Eclipse used for development.


****NOTE****

The EC2 server hosted a Java servlet(that I also wrote) and converted it to use for fingerprint authentication. It then sent it back to the phone to display. The particular server is no longer running so the apk generated wouldn't work, but this code sample gives a chance to see how the app was written. Also, the innerID SDK license has been removed from this code sample as well as the compiled C library for innerID. This repository is to show Android development sample code.
