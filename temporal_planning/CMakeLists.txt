cmake_minimum_required(VERSION 3.5)
project(temporal_planning)


find_package(ament_cmake REQUIRED)
find_package(rclcpp REQUIRED)
find_package(rclcpp_action REQUIRED)
find_package(plansys2_msgs REQUIRED)
find_package(plansys2_executor REQUIRED)

set(CMAKE_CXX_STANDARD 17)

set(dependencies
    rclcpp
    rclcpp_action
    plansys2_msgs
    plansys2_executor
)


add_executable(move_agent_action_node src/move_agent_action_node.cpp)
ament_target_dependencies(move_agent_action_node ${dependencies})

add_executable(move_agent_and_carrier_action_node src/move_agent_and_carrier_action_node.cpp)
ament_target_dependencies(move_agent_and_carrier_action_node ${dependencies})

add_executable(fill_box_and_load_it_on_carrier_action_node src/fill_box_and_load_it_on_carrier_action_node.cpp)
ament_target_dependencies(fill_box_and_load_it_on_carrier_action_node ${dependencies})

add_executable(unload_box_deliver_its_content_and_reload_it_on_carrier_action_node src/unload_box_deliver_its_content_and_reload_it_on_carrier_action_node.cpp)
ament_target_dependencies(unload_box_deliver_its_content_and_reload_it_on_carrier_action_node ${dependencies})

add_executable(unload_empty_box_from_carrier_action_node src/unload_empty_box_from_carrier_action_node.cpp)
ament_target_dependencies(unload_empty_box_from_carrier_action_node ${dependencies})

install(DIRECTORY launch pddl DESTINATION share/${PROJECT_NAME})

install(TARGETS
  move_agent_action_node
  move_agent_and_carrier_action_node
  fill_box_and_load_it_on_carrier_action_node
  unload_box_deliver_its_content_and_reload_it_on_carrier_action_node
  unload_empty_box_from_carrier_action_node
  ARCHIVE DESTINATION lib
  LIBRARY DESTINATION lib
  RUNTIME DESTINATION lib/${PROJECT_NAME}
)

if(BUILD_TESTING)
  find_package(ament_lint_auto REQUIRED)
  ament_lint_auto_find_test_dependencies()

  find_package(ament_cmake_gtest REQUIRED)
endif()

ament_export_dependencies(${dependencies})

ament_package()
