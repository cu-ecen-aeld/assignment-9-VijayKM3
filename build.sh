#!/bin/bash
# Script to build image for qemu.
# Author: Siddhant Jajoo.

# Force Yocto-compatible umask for CI
umask 0002

git submodule init
git submodule sync
git submodule update

# local.conf won't exist until this step on first execution
source poky/oe-init-build-env

# --- Ensure bblayers.conf is valid ---
if [ ! -f conf/bblayers.conf ]; then
    echo "Creating default bblayers.conf from sample"
    cp ../poky/meta-poky/conf/bblayers.conf.sample conf/bblayers.conf
fi

# Ensure required layers exist in bblayers.conf
grep "../poky/meta " conf/bblayers.conf >/dev/null || \
    sed -i '/BBLAYERS ?=/a\  ${TOPDIR}/../poky/meta \\' conf/bblayers.conf
grep "../poky/meta-poky " conf/bblayers.conf >/dev/null || \
    sed -i '/BBLAYERS ?=/a\  ${TOPDIR}/../poky/meta-poky \\' conf/bblayers.conf
grep "../poky/meta-yocto-bsp " conf/bblayers.conf >/dev/null || \
    sed -i '/BBLAYERS ?=/a\  ${TOPDIR}/../poky/meta-yocto-bsp \\' conf/bblayers.conf
grep "../meta-aesd " conf/bblayers.conf >/dev/null || \
    sed -i '/BBLAYERS ?=/a\  ${TOPDIR}/../meta-aesd \\' conf/bblayers.conf

# Replace auto-generated local.conf with repo-tracked template
if [ -f ../meta-aesd/conf/local.conf.sample ]; then
    echo "Using repo-tracked local.conf.sample"
    cp ../meta-aesd/conf/local.conf.sample conf/local.conf
else
    echo "WARNING: local.conf.sample not found, using default generated one"
fi

CONFLINE="MACHINE = \"qemuarm64\""

cat conf/local.conf | grep "${CONFLINE}" > /dev/null
local_conf_info=$?

if [ $local_conf_info -ne 0 ];then
	echo "Append ${CONFLINE} in the local.conf file"
	echo ${CONFLINE} >> conf/local.conf
	
else
	echo "${CONFLINE} already exists in the local.conf file"
fi


bitbake-layers show-layers | grep "meta-aesd" > /dev/null
layer_info=$?

if [ $layer_info -ne 0 ];then
	echo "Adding meta-aesd layer"
	bitbake-layers add-layer ../meta-aesd
else
	echo "meta-aesd layer already exists"
fi

set -e
bitbake core-image-aesd
