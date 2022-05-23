cmake_minimum_required (VERSION 3.8)
set (CMAKE_CXX_COMPILER "gcc")
set (CMAKE_C_COMPILER "gcc")
set (CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

project ("linux_utils")

FIND_PACKAGE(JNI REQUIRED)
find_package(X11 REQUIRED)

include_directories(${JNI_INCLUDE_DIRS})
include_directories(${X11_INCLUDE_DIR})

add_library(linux_utils SHARED "linux_utils.cpp" "linux_utils.h")
target_link_libraries(linux_utils -lX11 -lGL)

