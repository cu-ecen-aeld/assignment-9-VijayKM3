# meta-aesd/recipes-ldd/misc-modules/misc-modules.bb
SUMMARY = "LDD3 misc-modules (e.g., hello, jit, etc.) + init script"
HOMEPAGE = "https://lwn.net/Kernel/LDD3/"
LICENSE = "GPL-2.0-only"
#LIC_FILES_CHKSUM = "file://misc-modules/hello.c;beginline=1;endline=20;md5=c13c9a08f10630b44d83bc840f94a920"

LIC_FILES_CHKSUM = "file://COPYING;md5=779576fcdeab908dd6114ea1769bc64b"

#SRC_URI = "git:///home/vijaykum/Assignment7;branch=main

SRC_URI = "git://github.com/cu-ecen-aeld/assignment-7-VijayKM3.git;protocol=https;branch=main \
           file://misc-modules.init "
           
#SRCREV = "5c3cae6ddc96b8645dfa6f6bc4ddbba08aae8789"
#SRCREV = "a7f1d65143749ecbfd6b33bf827da6d77295a5b3"
#SRCREV = "b6cbd2595c0796dc8ee236c8c57c70c75843222c"
#SRCREV = "0d913e0abbdfb81d698c0880e8bdc9f3199896a3"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

#inherit module update-rc.d
inherit update-rc.d

# Point Kbuild at misc-modules
#EXTRA_OEMAKE += " -C ${STAGING_KERNEL_DIR} M=${S}/misc-modules"
#EXTRA_OEMAKE += " -C ${STAGING_KERNEL_BUILDDIR} M=${S}/misc-modules"
#EXTRA_OEMAKE += " -C ${STAGING_KERNEL_BUILDDIR} M=${S}/misc-modules \
#                 ARCH=${TARGET_ARCH} CROSS_COMPILE=${TARGET_PREFIX}"
EXTRA_OEMAKE += " -C ${STAGING_KERNEL_BUILDDIR} M=${S}/misc-modules \
                 ARCH=arm64 CROSS_COMPILE=${TARGET_PREFIX}"

MODULES_MODULE_SYMVERS_LOCATION = "${STAGING_KERNEL_BUILDDIR}/Module.symvers"

do_compile() {
    oe_runmake modules
}

do_install() {
    
    # Install modules into the versioned kernel dir if possible.
    # Prefer KERNEL_VERSION but don't rely on it being set at parse time.
    KV="${KERNEL_VERSION}"
    if [ -z "${KV}" ]; then
        KV="$(sed -n 's/.*UTS_RELEASE "\(.*\)".*/\1/p' ${STAGING_KERNEL_BUILDDIR}/include/generated/utsrelease.h 2>/dev/null || true)"
    fi
    # If still empty, fall back to the WORKDIR's kernel build release if present
    if [ -z "${KV}" ] && [ -f "${STAGING_KERNEL_BUILDDIR}/include/config/kernel.release" ]; then
        KV="$(cat ${STAGING_KERNEL_BUILDDIR}/include/config/kernel.release)"
    fi

    # If still empty, install into a versioned path anyway (will expand to /lib/modules//... if empty)
    TARGET_DIR="${D}${nonarch_base_libdir}/modules/${KV}/extra"
    install -d "${TARGET_DIR}"

    for ko in ${S}/misc-modules/*.ko; do
        if [ -f "${ko}" ]; then
            install -m 0644 "${ko}" "${TARGET_DIR}/"
        fi
    done

    # remove any .debug dirs in both versioned and unversioned locations to avoid QA debug-file errors
  #  rm -rf "${D}${nonarch_base_libdir}/modules/"*/extra/.debug || true
  #  rm -rf "${D}${nonarch_base_libdir}/modules/extra/.debug" || true

    # Install init script
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/misc-modules.init ${D}${sysconfdir}/init.d/misc-modules
    
}

# Bundle everything in one package
PACKAGES = "${PN}"

FILES:${PN} = " \
    ${nonarch_base_libdir}/modules/*/extra \
    ${nonarch_base_libdir}/modules/*/extra/* \
    ${sysconfdir}/init.d/misc-modules \
"
PACKAGES =+ "${PN}-dbg"
FILES:${PN}-dbg += " \
    ${nonarch_base_libdir}/modules/*/extra/.debug \
    ${nonarch_base_libdir}/modules/*/extra/.debug/* \
"

#FILES:${PN} = " \
#    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra \
#    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/* \
#    ${nonarch_base_libdir}/modules/*/extra \
#    ${nonarch_base_libdir}/modules/*/extra/* \
#    ${nonarch_base_libdir}/modules/extra \
#    ${nonarch_base_libdir}/modules/extra/* \
#    ${sysconfdir}/init.d/misc-modules \
#"

#RDEPENDS:${PN} += "update-rc.d kernel-modules"
RDEPENDS:${PN} += "update-rc.d"

INITSCRIPT_NAME = "misc-modules"
INITSCRIPT_PARAMS = "defaults 98"

