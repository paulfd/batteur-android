# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
# Using C++ 17 conflicts because we're gonna assume that the filesystem library is there...
SET(CMAKE_CXX_STANDARD 14)
SET(CMAKE_BUILD_TYPE Release)
# Force LTO
set(ENABLE_LTO ON CACHE BOOL "" FORCE)

SET(SFIZZ_LV2 OFF CACHE BOOL "Disable sfizz's LV2")
SET(SFIZZ_VST OFF CACHE BOOL "Disable sfizz's VST")
SET(SFIZZ_LV2_UI OFF CACHE BOOL "Disable sfizz's LV2 UI")
SET(SFIZZ_RENDER OFF CACHE BOOL "Disable sfizz render")
SET(ENABLE_LTO OFF CACHE BOOL "Disable sfizz LTO")
SET(SFIZZ_JACK OFF CACHE BOOL "Disable sfizz jack")
SET(SFIZZ_SHARED OFF CACHE BOOL "disable sfizz shared library")
set(SFIZZ_USE_SNDFILE OFF CACHE BOOL "" FORCE)

add_subdirectory(sfizz)

set(BATTEUR_DIR batteur)
add_library(batteur STATIC
    ${BATTEUR_DIR}/src/BeatDescription.cpp
    ${BATTEUR_DIR}/src/FileReadingHelpers.cpp
    ${BATTEUR_DIR}/src/Player.cpp
    ${BATTEUR_DIR}/src/fmidi/fmidi_mini.cpp
    ${BATTEUR_DIR}/src/wrapper.cpp
)
target_compile_definitions(batteur PUBLIC "FMIDI_STATIC=1" "FMIDI_DISABLE_DESCRIBE_API=1")
target_include_directories(batteur PUBLIC ${BATTEUR_DIR}/src)

set (OBOE_DIR ./oboe)
add_subdirectory (${OBOE_DIR} ./oboe)

# Debug utilities
set (DEBUG_UTILS_PATH "${OBOE_DIR}/samples/debug-utils")
set (DEBUG_UTILS_SOURCES ${DEBUG_UTILS_PATH}/trace.cpp)

add_library(native-lib SHARED native-lib.cpp SoundEngine.cpp ${DEBUG_UTILS_SOURCES} )
target_include_directories (native-lib PRIVATE ${OBOE_DIR}/include)
target_include_directories(native-lib PRIVATE ${DEBUG_UTILS_PATH})

find_library(log-lib log)
target_link_libraries(native-lib ${log-lib} oboe batteur sfizz::sfizz)