LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

ifeq (userdebug,$(TARGET_BUILD_VARIANT))
LOCAL_DEX_PREOPT:=false
endif

LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_MODULE_OWNER := mtk

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.dlna
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.dm
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.exoplayerlib
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.mtkaudiopatchmanager
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.PhotoRender
LOCAL_STATIC_JAVA_LIBRARIES += sysprop-mediatek-vendor

LOCAL_STATIC_ANDROID_LIBRARIES := \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    android-support-v7-appcompat \
    android-support-v14-preference \
    android-support-v17-leanback \
    android-arch-lifecycle-extensions

LOCAL_STATIC_JAVA_LIBRARIES += \
    com.mediatek.support.sharecode \
    com.mediatek.support.tv \
    com.mediatek.network

LOCAL_USE_AAPT2 := true

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src)
# LOCAL_SRC_FILES += ../../../../public_inc/android/common/FileSystemPath.java
#LOCAL_SRC_FILES += ../../../../../../vendor/mediatek/tv/public_inc/android/common/FileSystemPath.java
ifneq (,$(filter %-cn,$(MTK_ANDROID_VERSION)))
# for CN
LOCAL_SRC_FILES += \
    aosp/Cn.java

# Samba
LOCAL_STATIC_JAVA_LIBRARIES += \
    com.mstar.android.ext
LOCAL_REQUIRED_MODULES += \
    libsamba_jni

# Image player
LOCAL_STATIC_JAVA_LIBRARIES += \
    com.mstar.android.ext \
    com.mstar.android

else
# for WW
LOCAL_STATIC_JAVA_LIBRARIES += jcifs

# dummy
LOCAL_SRC_FILES += \
    aosp/samba/java/com/mstar/android/HttpBean.java \
    aosp/samba/java/com/mstar/android/NanoHTTPD.java \
		aosp/samba/java/com/mstar/android/OnRecvMsg.java \
    aosp/samba/java/com/mstar/android/OnRecvMsgListener.java \
    aosp/samba/java/com/mstar/android/SambaFile.java \
		aosp/samba/java/com/mstar/android/SmbAuthentication.java \
    aosp/samba/java/com/mstar/android/SmbClient.java \
    aosp/samba/java/com/mstar/android/SmbDevice.java \
    aosp/samba/java/com/mstar/android/SmbShareFolder.java \
    aosp/samba/java/com/mstar/android/SambaStorageManager.java \
    aosp/media/java/com/mstar/android/media/MMediaPlayer.java

endif

LOCAL_PRIVATE_PLATFORM_APIS := true
#LOCAL_SDK_VERSION = current
LOCAL_PRIVILEGED_MODULE := true
LOCAL_REQUIRED_MODULES:= libpicapi
LOCAL_PACKAGE_NAME := MultiMediaPlayerArcelik
LOCAL_OVERRIDES_PACKAGES := MultiMediaPlayer
LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_USES_LIBRARIES := javax.obex

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
