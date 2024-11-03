if [ $# -ne 1 ]; then
	echo "Takes only one argument: a number in {1, 2, 3}, corresponding to the problem instance need to be solved"
	exit 1
fi

problem=0
if [ $1 -eq 1 ]; then
	problem="./pddl/problem1.pddl"

elif [ $1 -eq 2 ]; then
    problem="./pddl/problem2.pddl"

elif [ $1 -eq 3 ]; then
    problem="./pddl/problem3.pddl"

else
	echo "Given problem does not exist."
	exit 1
fi

javac -d ./bin -classpath "./src:./lib/pddl4j-4.0.0.jar:" ./src/*.java
java -classpath "./bin:./lib/pddl4j-4.0.0.jar:" MyAlgorithm "pddl/domain.pddl" $problem

