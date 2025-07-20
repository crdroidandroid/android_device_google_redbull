#
# Copyright (C) 2023 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Lineage Health
include hardware/google/pixel/lineage_health/device.mk

$(call soong_config_set,lineage_health,charging_control_supports_deadline,1)
