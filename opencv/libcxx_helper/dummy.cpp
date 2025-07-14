#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_spotsaverscanner_NativeUtils_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string message = "OpenCV JNI is working!";
    return env->NewStringUTF(message.c_str());
}
