
#include <stdio.h>
#include <jni.h>
#include <jni_md.h>
#include "org_beifengtz_jvmm_core_OsProvider.h"

void hello(void) {
    printf("Hello, World!\n");
}

JNIEXPORT void JNICALL Java_org_beifengtz_jvmm_core_OsProvider_sayHello(JNIEnv *env, jobject ref) {
    hello();
}

JNIEXPORT jstring JNICALL Java_org_beifengtz_jvmm_core_OsProvider_load(JNIEnv *env, jclass ref) {
    const char *str = "hello, world";
    return (*env)->NewStringUTF(env, str);
}

