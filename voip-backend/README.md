# Local Caller backend

This server is used by the Android app in the PrankSmsSimulator project.

It places an outbound PSTN call through Twilio. The caller ID is not supplied by the Android app. It is the TWILIO_FROM number configured on the server.

## Required environment variables

- TWILIO_ACCOUNT_SID
- TWILIO_AUTH_TOKEN
- TWILIO_FROM
- TWILIO_VOICE_URL

TWILIO_FROM must be a Twilio number on the account or a caller ID that Twilio has verified for outbound voice. Twilio does not allow the app to submit an arbitrary unverified number.

TWILIO_VOICE_URL should be an HTTPS TwiML URL that tells Twilio what the call should do after the recipient answers.

## Run

    npm install
    npm start

The Android app should use:

    https://YOUR-SERVER/call

Do not put the Twilio Auth Token in the Android APK or Git repository.

Only use a caller ID that you are authorized to use. A local number can be provisioned through the telephony provider for the desired area code and then configured as TWILIO_FROM.
