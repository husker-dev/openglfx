cmake_minimum_required (VERSION 3.8)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

project ("openglfx")

set(BaseFiles "src/openglfx.cpp" "src/openglfx.h" "src/renderdoc_app.h" "src/renderdoc.cpp")
set(WindowsFiles "src/dxinterop.cpp" )
set(MacOSFiles "src/iosurface.cpp" )

set(JAVA_AWT_LIBRARY NotNeeded)
set(JAVA_JVM_LIBRARY NotNeeded)
set(JAVA_AWT_INCLUDE_PATH NotNeeded)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Linux
if (UNIX AND NOT APPLE)
    message(Compile x86)
    add_library(openglfx-x86 SHARED ${BaseFiles})
    target_link_libraries(openglfx-x86 -lX11 -lGL)
    set_target_properties(openglfx-x86 PROPERTIES PREFIX "")
    set_target_properties(openglfx-x86 PROPERTIES COMPILE_FLAGS "-m32" LINK_FLAGS "-m32")

    message(Compile x64)
    add_library(openglfx-x64 SHARED ${BaseFiles})
    target_link_libraries(openglfx-x64 -lX11 -lGL)
    set_target_properties(openglfx-x64 PROPERTIES PREFIX "")
    set_target_properties(openglfx-x64 PROPERTIES COMPILE_FLAGS "-m64" LINK_FLAGS "-m64")
endif()

# Windows
if (WIN32)
    if(X64)
        message(Compile x64)
        add_library(openglfx-x64 SHARED ${BaseFiles} ${WindowsFiles})
        target_link_libraries(openglfx-x64 INTERFACE "/NODEFAULTLIB")
    elseif(X86)
        message(Compile x86)
        add_library(openglfx-x86 SHARED ${BaseFiles} ${WindowsFiles})
        target_link_libraries(openglfx-x86 INTERFACE "/NODEFAULTLIB")
    endif()
endif()

# MacOS
if (APPLE)
    add_library(openglfx SHARED ${BaseFiles} ${MacOSFiles})
    target_link_libraries(openglfx "-framework CoreFoundation")
    target_link_libraries(openglfx "-framework IOSurface")
    target_link_libraries(openglfx "-framework OpenGL")
    set_target_properties(openglfx PROPERTIES PREFIX "")
endif()




