# DocsHelper

## How to build

1. [OpenCV](https://sourceforge.net/projects/opencvlibrary/files/4.6.0/opencv-4.6.0-android-sdk.zip/download) 다운로드 후 압축 해제

2. `OpenCV-android-sdk/sdk` 디렉토리를 `android`에 복사

3. `android/sdk/build.gradle`의 `compileSdkVersion`, `minSdkVersion`, `targetSdkVersion`를 `android/app/build.gradle`와 동일하게 수정

4. [ko_PP-OCRv3_rec.nb](https://drive.google.com/file/d/1Zaagx6j_pe0Kaj4Wrq7oFqVmUzCf_eH4/view?usp=share_link)와 [ko_PP-OCRv3_det.nb](https://drive.google.com/file/d/1aHqzOXvlnBjH0X-tDT9dR9yNyPGGVhQa/view?usp=share_link), [cls.nb](https://drive.google.com/file/d/1cF-Ap6UATvT0J9dfRW651mpqxp_evUrR/view?usp=share_link)를 다운로드 후 `android/app/src/main/assets/models/ko_PP-OCRv3`에 복사 (디렉토리 없는 경우 생성 필요)

5. Android Studio의 `Tools->SDK Manager->SDK Tools`에서 `CMake`와 `NDK` 설치

6. 정상적으로 빌드된 경우 디렉토리 구조는 다음과 같다.

```
├── README.md
├── PaddleLite
├── sdk
├── app
│   ├── build.gradle
│   ├── cache
│   ├── google-services.json
│   ├── proguard-rules.pro
│   ├── sampledata
│   └── src
│       ├── androidTest
│       ├── main
│       │   ├── AndroidManifest.xml
│       │   ├── assets
│       │   │   ├── asfont
│       │   │   ├── labels
│       │   │   └── models
│       │   │       └── ko_PP-OCRv3
│       │   │           ├── cls.nb
│       │   │           ├── ko_PP-OCRv3_det.nb
│       │   │           └── ko_PP-OCRv3_rec.nb
│       │   ├── cpp
│       │   ├── java
│       │   └── res
│       └── test
├── build.gradle
├── gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
└── settings.gradle
```
