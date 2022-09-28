cmake_minimum_required(VERSION 3.23)
project(jvmm_sysio C)

set(CMAKE_C_STANDARD 99)

find_package(JNI REQUIRED)

include_directories(${JNI_INCLUDE_DIRS})
include_directories(include)

add_library(jvmm_sysio SHARED src/library.c)
