# meta-aesd/recipes-ldd/scull/scull.bb
SUMMARY = "LDD3 scull character driver + init script"
HOMEPAGE = "https://lwn.net/Kernel/LDD3/"
LICENSE = "GPL-2.0-only"
#LIC_FILES_CHKSUM = "file://scull/main.c;beginline=1;endline=20;md5=4bb0bb986fc30db545e5cbaa058f3d9a"

LIC_FILES_CHKSUM = "file://COPYING;md5=779576fcdeab908dd6114ea1769bc64b"

#SRC_URI = "git:///home/vijaykum/Assignment7;branch=main 

SRC_URI = "git://github.com/cu-ecen-aeld/assignment-7-VijayKM3.git;protocol=https;branch=main \
           file://0001-Restrict-build-to-scull-and-misc-modules-only.patch \
           file://scull.init "
           
#SRCREV = "5c3cae6ddc96b8645dfa6f6bc4ddbba08aae8789"
#SRCREV = "a7f1d65143749ecbfd6b33bf827da6d77295a5b3"
#SRCREV = "b6cbd2595c0796dc8ee236c8c57c70c75843222c"
#SRCREV = "0d913e0abbdfb81d698c0880e8bdc9f3199896a3"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit module update-rc.d
#inherit update-rc.d

# Build only scull via Kbuild against the kernel in STAGING_KERNEL_DIR
#EXTRA_OEMAKE += " -C ${STAGING_KERNEL_DIR} M=${S}/scull"
EXTRA_OEMAKE += " -C ${STAGING_KERNEL_BUILDDIR} M=${S}/scull \
                 ARCH=arm64 CROSS_COMPILE=${TARGET_PREFIX}"
                 
MODULES_MODULE_SYMVERS_LOCATION = "${STAGING_KERNEL_BUILDDIR}/Module.symvers"

# Yocto's 'module' class will run oe_runmake by default, but we keep it explicit:
do_compile() {
    oe_runmake modules
}

# Install the scull.ko and the init script
do_install() {
    install -d ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra
    
    for ko in ${B}/scull/*.ko; do
        [ -f "$ko" ] && install -m 0644 "$ko" ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/
    done
    
    #install -m 0644 ${S}/scull/scull.ko ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/

    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/scull.init ${D}${sysconfdir}/init.d/scull
}

PACKAGES = "${PN}"

#FILES:${PN} += " \
#    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/scull.ko \
#    ${sysconfdir}/init.d/scull \
#"

FILES:${PN} += "${sysconfdir}/init.d/scull"

PACKAGES =+ "${PN}-dbg"
FILES:${PN}-dbg += " \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/.debug \
    ${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/.debug/* \
"

#RDEPENDS:${PN} += "update-rc.d kernel-modules"
RDEPENDS:${PN} += "update-rc.d"

# Hook into SysV init
INITSCRIPT_NAME = "scull"
INITSCRIPT_PARAMS = "defaults 99"

