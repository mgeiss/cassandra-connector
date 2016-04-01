#!/usr/bin/env bash
#
# Copyright 2016 Markus Geiss.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This scripts utilizes ccm, a Cassandra cluster manager to create a cluster
# for test purposes. ccm can be found at https://github.com/pcmanus/ccm.
#
# Prior starting a ccm cluster make sure needed loopback IP aliases exist.
# For RHEL/CentOS/Fedora based system simply execute:
#
sudo ifconfig lo:1 127.0.0.2 netmask 255.0.0.0 up
sudo ifconfig lo:2 127.0.0.3 netmask 255.0.0.0 up
ccm create mage_staging_cluster -v 2.2.5
ccm populate -n 3
ccm start
ccm node1 cqlsh

