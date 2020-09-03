# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
SET(CMAKE_CXX_STANDARD 14)
SET(CMAKE_BUILD_TYPE RelWithDebInfo)
SET(OGG_DIR ogg)
configure_file(${OGG_DIR}/include/ogg/config_types.h.in ${OGG_DIR}/include/ogg/config_types.h @ONLY)
add_library(ogg STATIC
        ${OGG_DIR}/src/framing.c
        ${OGG_DIR}/src/bitwise.c
        ${OGG_DIR}/src/crctable.h
)
target_include_directories(ogg PUBLIC ${OGG_DIR}/include)

SET(VORBIS_DIR vorbis)
add_library(vorbis STATIC
    ${VORBIS_DIR}/lib/mdct.c
    ${VORBIS_DIR}/lib/smallft.c
    ${VORBIS_DIR}/lib/block.c
    ${VORBIS_DIR}/lib/envelope.c
    ${VORBIS_DIR}/lib/window.c
    ${VORBIS_DIR}/lib/lsp.c
    ${VORBIS_DIR}/lib/lpc.c
    ${VORBIS_DIR}/lib/analysis.c
    ${VORBIS_DIR}/lib/synthesis.c
    ${VORBIS_DIR}/lib/psy.c
    ${VORBIS_DIR}/lib/info.c
    ${VORBIS_DIR}/lib/floor1.c
    ${VORBIS_DIR}/lib/floor0.c
    ${VORBIS_DIR}/lib/res0.c
    ${VORBIS_DIR}/lib/mapping0.c
    ${VORBIS_DIR}/lib/registry.c
    ${VORBIS_DIR}/lib/codebook.c
    ${VORBIS_DIR}/lib/sharedbook.c
    ${VORBIS_DIR}/lib/lookup.c
    ${VORBIS_DIR}/lib/bitrate.c
)
target_link_libraries(vorbis PRIVATE ogg)
target_include_directories(vorbis PUBLIC ${VORBIS_DIR}/include)

set(FLAC_DIR flac)
add_library(FLAC
    ${FLAC_DIR}/src/libFLAC/bitmath.c
    ${FLAC_DIR}/src/libFLAC/bitreader.c
    ${FLAC_DIR}/src/libFLAC/bitwriter.c
    ${FLAC_DIR}/src/libFLAC/cpu.c
    ${FLAC_DIR}/src/libFLAC/crc.c
    ${FLAC_DIR}/src/libFLAC/fixed.c
    ${FLAC_DIR}/src/libFLAC/fixed_intrin_sse2.c
    ${FLAC_DIR}/src/libFLAC/fixed_intrin_ssse3.c
    ${FLAC_DIR}/src/libFLAC/float.c
    ${FLAC_DIR}/src/libFLAC/format.c
    ${FLAC_DIR}/src/libFLAC/lpc.c
    ${FLAC_DIR}/src/libFLAC/lpc_intrin_sse.c
    ${FLAC_DIR}/src/libFLAC/lpc_intrin_sse2.c
    ${FLAC_DIR}/src/libFLAC/lpc_intrin_sse41.c
    ${FLAC_DIR}/src/libFLAC/lpc_intrin_avx2.c
    ${FLAC_DIR}/src/libFLAC/lpc_intrin_vsx.c
    ${FLAC_DIR}/src/libFLAC/md5.c
    ${FLAC_DIR}/src/libFLAC/memory.c
    ${FLAC_DIR}/src/libFLAC/metadata_iterators.c
    ${FLAC_DIR}/src/libFLAC/metadata_object.c
    ${FLAC_DIR}/src/libFLAC/stream_decoder.c
    ${FLAC_DIR}/src/libFLAC/stream_encoder.c
    ${FLAC_DIR}/src/libFLAC/stream_encoder_intrin_sse2.c
    ${FLAC_DIR}/src/libFLAC/stream_encoder_intrin_ssse3.c
    ${FLAC_DIR}/src/libFLAC/stream_encoder_intrin_avx2.c
    ${FLAC_DIR}/src/libFLAC/stream_encoder_framing.c
    ${FLAC_DIR}/src/libFLAC/window.c
    ${FLAC_DIR}/src/libFLAC/ogg_decoder_aspect.c
    ${FLAC_DIR}/src/libFLAC/ogg_encoder_aspect.c
    ${FLAC_DIR}/src/libFLAC/ogg_helper.c
    ${FLAC_DIR}/src/libFLAC/ogg_mapping.c
)
target_include_directories(FLAC PUBLIC ${FLAC_DIR}/include)
target_link_libraries(FLAC PRIVATE ogg)
target_compile_definitions(FLAC PRIVATE FLAC__NO_ASM)
target_compile_definitions(FLAC PRIVATE FLAC__NO_DLL)

SET(BUILD_PROGRAMS OFF CACHE BOOL "Disable sndfile programs")
SET(BUILD_EXAMPLES OFF CACHE BOOL "Disable sndfile examples")
SET(BUILD_TESTING OFF CACHE BOOL "Disable sndfile testing")
SET(WITH_STACK_PROTECTOR OFF CACHE BOOL "Disable flac stack protector")
add_subdirectory(libsndfile)

SET(SFIZZ_LV2 OFF CACHE BOOL "Disable sfizz's LV2")
SET(SFIZZ_RENDER OFF CACHE BOOL "Disable sfizz render")
SET(ENABLE_LTO OFF CACHE BOOL "Disable sfizz LTO")
SET(SFIZZ_JACK OFF CACHE BOOL "Disable sfizz jack")
SET(SFIZZ_SHARED OFF CACHE BOOL "disable sfizz shared library")
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

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp SoundEngine.cpp ${DEBUG_UTILS_SOURCES} )
target_include_directories (native-lib PRIVATE ${OBOE_DIR}/include)
target_include_directories(native-lib PRIVATE ${DEBUG_UTILS_PATH})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                        oboe batteur sfizz::sfizz)