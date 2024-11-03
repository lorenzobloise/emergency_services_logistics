colcon build --symlink-install
source install/setup.bash
ros2 launch temporal_planning temporal_planning_launch.py
