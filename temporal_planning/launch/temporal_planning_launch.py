# Copyright 2019 Intelligent Robotics Lab
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os

from ament_index_python.packages import get_package_share_directory

from launch import LaunchDescription
from launch.actions import DeclareLaunchArgument, IncludeLaunchDescription
from launch.launch_description_sources import PythonLaunchDescriptionSource
from launch.substitutions import LaunchConfiguration
from launch_ros.actions import Node


def generate_launch_description():
    # Get the launch directory
    directory = get_package_share_directory('temporal_planning')
    namespace = LaunchConfiguration('namespace')

    declare_namespace_cmd = DeclareLaunchArgument(
        'namespace',
        default_value='',
        description='Namespace')

    plansys2_cmd = IncludeLaunchDescription(
        PythonLaunchDescriptionSource(os.path.join(
            get_package_share_directory('plansys2_bringup'),
            'launch',
            'plansys2_bringup_launch_monolithic.py')),
        launch_arguments={
          'model_file': directory + '/pddl/domain_durative_actions.pddl',
          'namespace': namespace
          }.items())

    # Specify the actions
    move_agent_cmd = Node(
        package='temporal_planning',
        executable='move_agent_action_node',
        name='move_agent_action_node',
        namespace=namespace,
        output='screen',
        parameters=[])

    move_agent_and_carrier_cmd = Node(
        package='temporal_planning',
        executable='move_agent_and_carrier_action_node',
        name='move_agent_and_carrier_action_node',
        namespace=namespace,
        output='screen',
        parameters=[])

    fill_box_and_load_it_on_carrier_cmd = Node(
        package='temporal_planning',
        executable='fill_box_and_load_it_on_carrier_action_node',
        name='fill_box_and_load_it_on_carrier_action_node',
        namespace=namespace,
        output='screen',
        parameters=[])   
        
    unload_box_deliver_its_content_and_reload_it_on_carrier_cmd = Node(
        package='temporal_planning',
        executable='unload_box_deliver_its_content_and_reload_it_on_carrier_action_node',
        name='unload_box_deliver_its_content_and_reload_it_on_carrier_action_node',
        namespace=namespace,
        output='screen',
        parameters=[])
        
    unload_empty_box_from_carrier_cmd = Node(
        package='temporal_planning',
        executable='unload_empty_box_from_carrier_action_node',
        name='unload_empty_box_from_carrier_action_node',
        namespace=namespace,
        output='screen',
        parameters=[])   # Create the launch description and populate
        
    ld = LaunchDescription()

    ld.add_action(declare_namespace_cmd)

    # Declare the launch options
    ld.add_action(plansys2_cmd)

    ld.add_action(move_agent_cmd)
    ld.add_action(move_agent_and_carrier_cmd)
    ld.add_action(fill_box_and_load_it_on_carrier_cmd)
    ld.add_action(unload_box_deliver_its_content_and_reload_it_on_carrier_cmd)
    ld.add_action(unload_empty_box_from_carrier_cmd)

    return ld
