cmake_minimum_required(VERSION 3.4.1)
project(openglfx C)

include_directories(C:/Users/redfa/.jdks/corretto-11.0.12/include)
include_directories(C:/Users/redfa/.jdks/corretto-11.0.12/include/linux)
include_directories(C:/Users/redfa/.jdks/corretto-11.0.12/include/win32)

set(CMAKE_C_STANDARD 99)

add_library(openglfx SHARED com_husker_openglfx_utils_NativeWindowUtils.h com_husker_openglfx_utils_NativeWindowUtils.c)


