# 🚀 Kundali AI – Setup & Configuration Guide

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK with API 35
- Google Play Developer account
- Cerebras AI API key (https://cloud.cerebras.ai)
- Google AdMob account

---

## Step 1: Open in Android Studio

1. Clone or extract the project
2. Open Android Studio → `File > Open` → select `KundaliAI/` folder
3. Wait for Gradle sync to complete

---

## Step 2: Configure API Keys

### 2.1 Cerebras API Key
In `app/build.gradle`, replace:
```gradle
buildConfigField "String", "CEREBRAS_API_KEY", '"YOUR_CEREBRAS_API_KEY_HERE"'
```
With your actual Cerebras API key from https://cloud.cerebras.ai

### 2.2 AdMob App ID
Replace the test AdMob App ID in `app/build.gradle`:
```gradle
buildConfigField "String", "ADMOB_APP_ID", '"ca-app-pub-XXXXXXXXXX~XXXXXXXXXX"'
```
And update `res/values/strings.xml`:
```xml
<string name="admob_app_id">ca-app-pub-YOUR-REAL-ID</string>
```

### 2.3 AdMob Banner ID
In `app/build.gradle` release block:
```gradle
buildConfigField "String", "ADMOB_BANNER_ID", '"ca-app-pub-XXXXXXXXXX/XXXXXXXXXX"'
```

---

## Step 3: Google Play Billing Setup

### 3.1 Create In-App Product
1. Go to Google Play Console → Your App → Monetization → In-app products
2. Create a new **One-time product**:
   - Product ID: `kundali_pdf_unlock`
   - Name: "Kundali Full PDF Report"
   - Description: "Unlock complete Kundali PDF with detailed AI predictions"
   - Price: ₹49

### 3.2 Test Billing
- Add test accounts in Play Console → Setup → License testing
- Use test card details for sandbox testing

---

## Step 4: Add google-services.json
If using Firebase (optional for analytics):
1. Create Firebase project
2. Download `google-services.json`
3. Place in `app/` directory

---

## Step 5: Add Fonts
Download and add these fonts to `res/font/`:
- `playfair_display_bold.ttf` – From Google Fonts
- `lato_italic.ttf` – From Google Fonts

Or replace with any serif/elegant font pair.

---

## Step 6: Add Lottie Animation
Download a stars/cosmic loading animation from [LottieFiles](https://lottiefiles.com):
- Save as `res/raw/stars_loading.json`
- Search: "stars", "cosmos", "loading sparkle"

---

## Step 7: Update Legal URLs
In `utils/Constants.kt`:
```kotlin
const val PRIVACY_POLICY_URL = "https://YOURWEBSITE.com/privacy-policy"
const val TERMS_URL = "https://YOURWEBSITE.com/terms"
const val CONTACT_EMAIL = "your@email.com"
```

---

## Step 8: FileProvider (for PDF sharing)

Add to `AndroidManifest.xml` inside `<application>`:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

---

## Step 9: Build & Test

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Run on Device
Connect device with USB debugging enabled and run from Android Studio.

---

## Step 10: Play Store Submission

1. Generate signed APK/AAB:
   - `Build > Generate Signed Bundle/APK`
   - Create a new keystore (save it securely!)
   - Select `Release`

2. Upload to Play Console:
   - Internal testing → Closed testing → Open testing → Production

3. Fill in Play Store listing using `PLAY_STORE_LISTING.md`

4. Complete Data Safety form using information in `PLAY_STORE_LISTING.md`

5. Submit for review

---

## Cerebras API Usage Notes

The app uses the `gpt-oss-120b` model via:
```
POST https://api.cerebras.ai/v1/chat/completions
Authorization: Bearer YOUR_API_KEY
```

**Cost estimate:** ~$0.001-0.005 per Kundali generation (very low cost)

---

## Monetization Summary

| Revenue Stream | Details |
|----------------|---------|
| PDF Unlock | ₹49 one-time (Google Play) |
| Banner Ads | AdMob (per impression) |

---

## Support
- Email: support@kundaliai.app
- Documentation: See inline code comments

---

*Built with ❤️ for the Vedic astrology community*
