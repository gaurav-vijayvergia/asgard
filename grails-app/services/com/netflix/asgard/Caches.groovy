/*
 * Copyright 2012 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.asgard

import com.amazonaws.services.autoscaling.model.AutoScalingGroup
import com.amazonaws.services.autoscaling.model.LaunchConfiguration
import com.amazonaws.services.autoscaling.model.ScalingPolicy
import com.amazonaws.services.cloudwatch.model.MetricAlarm
import com.amazonaws.services.ec2.model.AvailabilityZone
import com.amazonaws.services.ec2.model.Image
import com.amazonaws.services.ec2.model.Instance
import com.amazonaws.services.ec2.model.KeyPairInfo
import com.amazonaws.services.ec2.model.ReservedInstances
import com.amazonaws.services.ec2.model.SecurityGroup
import com.amazonaws.services.ec2.model.Snapshot
import com.amazonaws.services.ec2.model.SpotInstanceRequest
import com.amazonaws.services.ec2.model.Volume
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription
import com.amazonaws.services.elasticloadbalancing.model.SourceSecurityGroup
import com.amazonaws.services.rds.model.DBInstance
import com.amazonaws.services.rds.model.DBSecurityGroup
import com.amazonaws.services.rds.model.DBSnapshot
import com.netflix.asgard.model.ApplicationInstance
import com.netflix.asgard.model.ApplicationMetrics
import com.netflix.asgard.model.HardwareProfile
import com.netflix.asgard.model.InstanceTypeData
import com.netflix.asgard.model.MultiRegionInstancePrices
import com.netflix.asgard.model.SimpleQueue
import com.netflix.asgard.model.TopicData
import com.netflix.asgard.push.Cluster

/**
 * By creating all these object upfront and letting other services initialize them only if needed, we remove the problem
 * of repeatedly needing to recreate and reload the caches during development of service classes.
 */
class Caches {

    CachedMap<AppRegistration> allApplications
    CachedMap<ApplicationMetrics> allApplicationMetrics
    CachedMap<HardwareProfile> allHardwareProfiles

    MultiRegionCachedMap<MetricAlarm> allAlarms
    MultiRegionCachedMap<ApplicationInstance> allApplicationInstances
    MultiRegionCachedMap<AutoScalingGroup> allAutoScalingGroups
    MultiRegionCachedMap<AvailabilityZone> allAvailabilityZones
    MultiRegionCachedMap<Cluster> allClusters
    MultiRegionCachedMap<DBInstance> allDBInstances
    MultiRegionCachedMap<DBSecurityGroup> allDBSecurityGroups
    MultiRegionCachedMap<DBSnapshot> allDBSnapshots
    MultiRegionCachedMap<String> allDomains
    MultiRegionCachedMap<FastProperty> allFastProperties
    MultiRegionCachedMap<Image> allImages
    MultiRegionCachedMap<Instance> allInstances
    MultiRegionCachedMap<InstanceTypeData> allInstanceTypes
    MultiRegionCachedMap<KeyPairInfo> allKeyPairs
    MultiRegionCachedMap<LaunchConfiguration> allLaunchConfigurations
    MultiRegionCachedMap<LoadBalancerDescription> allLoadBalancers
    MultiRegionCachedMap<SimpleQueue> allQueues
    MultiRegionCachedMap<ReservedInstances> allReservedInstancesGroups
    MultiRegionCachedMap<ScalingPolicy> allScalingPolicies
    MultiRegionCachedMap<SecurityGroup> allSecurityGroups
    MultiRegionCachedMap<Snapshot> allSnapshots
    MultiRegionCachedMap<SourceSecurityGroup> allSourceSecurityGroups
    MultiRegionCachedMap<SpotInstanceRequest> allSpotInstanceRequests
    MultiRegionCachedMap<TopicData> allTopics
    MultiRegionCachedMap<Volume> allVolumes

    MultiRegionInstancePrices allOnDemandPrices
    MultiRegionInstancePrices allReservedPrices
    MultiRegionInstancePrices allSpotPrices

    Caches(CachedMapBuilder cachedMapBuilder, ConfigService configService) {

        allClusters = cachedMapBuilder.of(EntityType.cluster).buildMultiRegionCachedMap()
        allAutoScalingGroups = cachedMapBuilder.of(EntityType.autoScaling, 120).buildMultiRegionCachedMap()
        allLaunchConfigurations = cachedMapBuilder.of(EntityType.launchConfiguration, 180).buildMultiRegionCachedMap()
        allLoadBalancers = cachedMapBuilder.of(EntityType.loadBalancer, 120).buildMultiRegionCachedMap()
        allSourceSecurityGroups = cachedMapBuilder.of(EntityType.sourceSecurityGroup).buildMultiRegionCachedMap()
        allAvailabilityZones = cachedMapBuilder.of(EntityType.availabilityZone, 3600).buildMultiRegionCachedMap()
        allKeyPairs = cachedMapBuilder.of(EntityType.keyPair).buildMultiRegionCachedMap()
        allImages = cachedMapBuilder.of(EntityType.image, 120).buildMultiRegionCachedMap()
        allInstances = cachedMapBuilder.of(EntityType.instance, 120).buildMultiRegionCachedMap()
        allSpotInstanceRequests = cachedMapBuilder.of(EntityType.spotInstanceRequest, 120).buildMultiRegionCachedMap()
        allApplicationInstances = cachedMapBuilder.of(EntityType.applicationInstance, 60).buildMultiRegionCachedMap()
        allReservedInstancesGroups = cachedMapBuilder.of(EntityType.reservation, 300).buildMultiRegionCachedMap()
        allSecurityGroups = cachedMapBuilder.of(EntityType.security, 120).buildMultiRegionCachedMap()
        allSnapshots = cachedMapBuilder.of(EntityType.snapshot, 300).buildMultiRegionCachedMap()
        allVolumes = cachedMapBuilder.of(EntityType.volume, 300).buildMultiRegionCachedMap()
        allDomains = cachedMapBuilder.of(EntityType.domain, 120).buildMultiRegionCachedMap()
        allTopics = cachedMapBuilder.of(EntityType.topic, 120).buildMultiRegionCachedMap()
        allQueues = cachedMapBuilder.of(EntityType.queue, 120).buildMultiRegionCachedMap()
        allAlarms = cachedMapBuilder.of(EntityType.alarm, 120).buildMultiRegionCachedMap()
        allDBInstances = cachedMapBuilder.of(EntityType.rdsInstance, 120).buildMultiRegionCachedMap()
        allDBSecurityGroups = cachedMapBuilder.of(EntityType.dbSecurity, 120).buildMultiRegionCachedMap()
        allDBSnapshots = cachedMapBuilder.of(EntityType.dbSnapshot, 120).buildMultiRegionCachedMap()
        allFastProperties = cachedMapBuilder.of(EntityType.fastProperty, 180).buildMultiRegionCachedMap(configService.
                platformServiceRegions)
        allScalingPolicies = cachedMapBuilder.of(EntityType.scalingPolicy, 120).buildMultiRegionCachedMap()
        allApplications = cachedMapBuilder.of(EntityType.application, 120).buildCachedMap()
        allApplicationMetrics = cachedMapBuilder.of(EntityType.applicationMetric, 120).buildCachedMap()

        // Use one thread for all instance type and pricing caches. None of these need updating more than once an hour.
        allHardwareProfiles = cachedMapBuilder.of(EntityType.hardwareProfile, 3600).buildCachedMap()
        allOnDemandPrices = MultiRegionInstancePrices.create('On Demand Prices')
        allReservedPrices = MultiRegionInstancePrices.create('Reserved Prices')
        allSpotPrices = MultiRegionInstancePrices.create('Spot Prices')
        allInstanceTypes = cachedMapBuilder.of(EntityType.instanceType).buildMultiRegionCachedMap()
    }
}