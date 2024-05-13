# SPDX-FileCopyrightText: 2023 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0

ifeq ($(PRODUCT_VIRTUAL_AB_OTA),true)
BOARD_PRODUCTIMAGE_MINIMAL_PARTITION_RESERVED_SIZE ?= true
endif

ifneq ($(WITH_GMS),true)
BOARD_PRODUCTIMAGE_EXTFS_INODE_COUNT ?= -1
BOARD_PRODUCTIMAGE_PARTITION_RESERVED_SIZE ?= 2557691392
BOARD_SYSTEMIMAGE_EXTFS_INODE_COUNT ?= -1
BOARD_SYSTEMIMAGE_PARTITION_RESERVED_SIZE ?= 94371840
BOARD_SYSTEM_EXTIMAGE_EXTFS_INODE_COUNT ?= -1
BOARD_SYSTEM_EXTIMAGE_PARTITION_RESERVED_SIZE ?= 94371840
endif