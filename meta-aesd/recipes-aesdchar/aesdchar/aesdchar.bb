DESCRIPTION = "AESD Character Driver"
PR = "r0"
#LICENSE = "GPL-2.0-only"
#LIC_FILES_CHKSUM = "file://COPYING;md5=b234ee4d69f5fce4486a80fdaf4a4263"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# to declare a dependency on the kernel headers
DEPENDS += "virtual/kernel"

# Replace with your real repo URL for aesdchar source
SRC_URI = "git://github.com/cu-ecen-aeld/assignments-3-and-later-VijayKM3.git;protocol=https;branch=main \
           file://aesdchar.init \
           "

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git/aesd-char-driver"

inherit module update-rc.d

EXTRA_OEMAKE += " -C ${STAGING_KERNEL_BUILDDIR} M=${S} ARCH=arm64 CROSS_COMPILE=${TARGET_PREFIX}"

#EXTRA_OEMAKE += "KERNELDIR=${STAGING_KERNEL_DIR}"
# Make both do_compile and do_install from module.bbclass run in the kernel build dir
#EXTRA_OEMAKE += " -C ${STAGING_KERNEL_BUILDDIR} M=${S}"

# do_compile() { oe_runmake modules; }

INITSCRIPT_NAME = "aesdchar"
INITSCRIPT_PARAMS = "defaults 99"

# ensure we have explicit packages: init-script package (PN), kernel-module, and kernel-module-dbg
PACKAGES = "${PN} kernel-module-aesdchar kernel-module-aesdchar-dbg"

# make sure the .ko and init script are packaged
#FILES:${PN} += " \
#    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/aesdchar.ko \
#    ${sysconfdir}/init.d/aesdchar \
#"

# init script belongs to main package
FILES:${PN} = " \
    ${sysconfdir}/init.d/aesdchar \
"

# kernel-module package gets only the .ko (no .debug)
FILES:kernel-module-aesdchar = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*.ko \
"

# debug package receives debug files under .debug
FILES:kernel-module-aesdchar-dbg = " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/.debug/* \
"

#FILES:${PN} += "${sysconfdir}/init.d/S98aesdchar"

# Package the kernel module under the conventional kernel-module-* package name

RPROVIDES:kernel-module-aesdchar += "kernel-module-aesdchar"
RDEPENDS:${PN} += "kernel-module-aesdchar-${KERNEL_VERSION}"
RPROVIDES:kernel-module-aesdchar += "kernel-module-aesdchar-${KERNEL_VERSION}"

# keep your init script install as an append if you want:
do_install() {
# ensure target dir
    TARGET_DIR="${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra"
    install -d "${TARGET_DIR}"

    # Copy the built module from build dir ${B} (preferred), or from ${S}
    if [ -f "${B}/aesdchar.ko" ]; then
        install -m 0644 "${B}/aesdchar.ko" "${TARGET_DIR}/"
    elif [ -f "${S}/aesdchar.ko" ]; then
        install -m 0644 "${S}/aesdchar.ko" "${TARGET_DIR}/"
    else
        bbnote "aesdchar.ko not found in ${B} or ${S}; listing for debugging"
        ls -la ${B} || true
        ls -la ${S} || true
        die "aesdchar.ko not found; build may have failed earlier (check do_compile)."
    fi

    # If build produced debug objects in ${B}/.debug, copy them into target .debug
    if [ -d "${B}/.debug" ]; then
        install -d "${TARGET_DIR}/.debug"
        cp -a "${B}/.debug/"* "${TARGET_DIR}/.debug/" || true
    fi

    # Also check for debug artifacts under ${B} like aesdchar.ko.debug etc.
    if [ -f "${B}/aesdchar.ko.debug" ]; then
        install -d "${TARGET_DIR}/.debug"
        cp -a "${B}/aesdchar.ko.debug" "${TARGET_DIR}/.debug/" || true
    fi

    # Install init script
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/aesdchar.init ${D}${sysconfdir}/init.d/aesdchar
    chmod 0755 ${D}${sysconfdir}/init.d/aesdchar || true
}


