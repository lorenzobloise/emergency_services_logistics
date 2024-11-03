import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.InvalidConfigurationException;
import fr.uga.pddl4j.planners.LogLevel;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openjdk.jol.info.GraphLayout;
import picocli.CommandLine;
import java.util.*;


public class MyAlgorithm extends AbstractPlanner {

    private static final Logger LOGGER = LogManager.getLogger(MyAlgorithm.class.getName());

    private int exploredNodes;

    public MyAlgorithm() {
        exploredNodes = 0;
    }

    public int getExploredNodes(){
        return exploredNodes;
    }

    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    @Override
    public Plan solve(Problem problem) {
        LOGGER.info("* Starting My ASTAR search with MYHEURISTIC heuristic \n");
        Plan plan = null;

        try {
            long begin = System.currentTimeMillis();
            plan = this.My_ASTAR(problem);
            long end = System.currentTimeMillis();

            if (plan != null) {
                LOGGER.info("* My ASTAR search with MYHEURISTIC heuristic succeeded\n");
                this.getStatistics().setTimeToSearch(end - begin);
            } else {
                LOGGER.info("* My ASTAR search with MYHEURISTIC heuristic failed\n");
            }
            return plan;

        } catch (ProblemNotSupportedException e) {
            LOGGER.error("Problem not supported");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Do an A star search to find a solution to the problem. This specific version of A star uses a frontier of
     * nodes ordered by 4 features:
     * <ul>
     *     <li>Number of 'move_agent' and 'move_agent_and_carrier' actions</li>
     *     <li>Number of 'fill_box_and_load_it_on_carrier' actions</li>
     *     <li>Number of 'unload_box_deliver_its_content_and_load_it_on_carrier' actions</li>
     * </ul>
     * Since the heuristic is not admissible, we prefer not to use it to order the frontier.
     *
     * The peculiarity of this search strategy is that, unlike the classic A star graph search, here is made an initial
     * pruning of the search tree: the best value calculated by the heuristic is stored; if the current node has a
     * better value of the heuristic, the frontier is completely emptied and the algorithm continues the search
     * exclusively along the direction provided by the current node.
     *
     * Also the adding of a node's successors to the frontier was modified: in fact, a successor is considered
     * eligible for insertion in the frontier if:
     * <ul>
     *     <il>It's not been already explored</il>
     *     <il>It has a value of the heuristic not greater than the double of the best heristic</il>
     * </ul>
     * So two cases can happen:
     * <ul>
     *     <li>No successor can be added: in this extreme occasion, we procede to add only a part of them, which
     *     becomes smaller while going further deep in the tree: initially some non-insertable nodes may be
     *     accepted, but the further we go with the search, the more we have to focus on the best nodes</li>
     *     <li>There are insertable nodes: we proceed to add only them to the frontier, ignoring the rest</li>
     * </ul>
     *
     * Thanks to these improvements, not only the algorithm manages to find a plan quickly, but finds an optimal
     * solution or close to optimality.
     *
     * @param problem the problem to solve
     * @return a plan for the problem
     * @throws ProblemNotSupportedException if the problem is not supported
     * */

    public Plan My_ASTAR(Problem problem) throws ProblemNotSupportedException {

        // Check if the planner supports the problem
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        // The heuristic used in the search
        MyHeuristic heuristic = new MyHeuristic(problem);

        // Initial state of the problem
        State init = new State(problem.getInitialState());

        // Set of nodes already explored
        Set<Node> alreadyExploredNodes = new HashSet<>();

        // The frontier (nodes that have to be explored), ordered by:
        // - Number of 'move' actions
        // - Number of 'fill' actions
        // - Number of 'unload_deliver_load' actions
        // - Cost
        PriorityQueue<Node> frontier = new PriorityQueue<>((n1, n2) -> {
            return (int)( n1.getTotalNumberOfMoveActions() - n2.getTotalNumberOfMoveActions()
                    - n1.getTotalNumberOfFillActions() + n2.getTotalNumberOfFillActions()
                    - n1.getTotalNumberOfDeliverActions() + n2.getTotalNumberOfDeliverActions()
                    + n1.getCost() - n2.getCost());
        });

        PriorityQueue<Node> newFrontier = null;

        // Matrix with useful information about the agents
        int[][] agentFacts = new int[heuristic.getNumAgents()][6];
        for(int i = 0; i < agentFacts.length; i++)
            agentFacts[i][0] = heuristic.getAgentAtIndex(i);

        // Matrix with useful information about the carriers
        int[][] carrierFacts = new int[heuristic.getNumCarriers()][2];
        for(int i = 0; i < carrierFacts.length; i++)
            carrierFacts[i][0] = heuristic.getCarrierAtIndex(i);

        // Mapping between the boxes and their content
        HashMap<Integer, Integer> boxFacts = new HashMap<>();

        // Root node from which the search begins
        Node root = new Node(init, null, 0, null, 0, agentFacts, carrierFacts, boxFacts);
        frontier.add(root);

        // The best heuristic found until now
        double bestHeuristic = heuristic.estimate(root);

        Node current = root;

        // Set the search timeout
        int timeout = this.getTimeout() * 1000;
        int time = 0;

        // Amount of memory used for the search
        long memoryUsedForSearch = 0;

        // Start the search
        while (!frontier.isEmpty() && time < timeout) {

            // Take the best node from the frontier
            current = frontier.poll();

            // Add the number of explored nodes
            exploredNodes++;

            // Insert the current node in the list of already explored nodes
            alreadyExploredNodes.add(current);

            // Compute the heuristic of the current node
            double currentHeuristic = heuristic.estimate(current);

            // If the heuristic is 0, it is a solution to the problem.
            // Then we extract the plan from it and return it, also save the amount of memory used for the search
            // and the number of explored nodes
            if (currentHeuristic == 0.0) {
                this.exploredNodes = alreadyExploredNodes.size();
                memoryUsedForSearch += GraphLayout.parseInstance(frontier).totalSize();
                this.getStatistics().setMemoryUsedToSearch(memoryUsedForSearch);
                return this.extractPlan(current);
            }
            else{

                // If the heuristic is not 0 but nonetheless is less than the smallest value found until now,
                // we can set this value as the new best heuristic and remove from the frontier those nodes
                // having a heuristic greater than the best. Emptying the frontier is an extreme operation, so it
                // must be done only when all the nodes have a greater value than the best.
                if(currentHeuristic < bestHeuristic){
                    bestHeuristic = currentHeuristic;

                    int capacity = 0;
                    for(Node node : frontier)
                        if(heuristic.estimate(node) <= bestHeuristic)
                            capacity++;

                    if(capacity == 0){
                        frontier.clear();
                    }
                    else{
                        newFrontier = new PriorityQueue<>(capacity, (n1, n2) -> {
                            return (int)( n1.getTotalNumberOfMoveActions() - n2.getTotalNumberOfMoveActions()
                                    - n1.getTotalNumberOfFillActions() + n2.getTotalNumberOfFillActions()
                                    - n1.getTotalNumberOfDeliverActions() + n2.getTotalNumberOfDeliverActions()
                                    + n1.getCost() - n2.getCost());
                        });

                        for(Node node : frontier)
                            if(heuristic.estimate(node) <= bestHeuristic)
                                newFrontier.add(node);

                        memoryUsedForSearch += GraphLayout.parseInstance(frontier).totalSize();
                        frontier = newFrontier;
                    }
                }

                // Successors of the current node
                ArrayList<Node> successors = new ArrayList<>();

                // List of index of the successors of the current node having an acceptable heuristic
                // (and thus can be inserted in the frontier)
                LinkedList<Integer> indexesOfInsertableSuccessors = new LinkedList<>();
                int insertableSuccessors = 0;

                // Take the actions of the problem and generate the successors
                for(Action a : problem.getActions()){

                    // If the action is applicable to the current state
                    if (a.isApplicable(current)) {

                        // Generate a new state
                        State nextState = new State(current);

                        // Apply to this state the effects of the action
                        List<ConditionalEffect> effects = a.getConditionalEffects();
                        for (ConditionalEffect ce : effects) {
                            if (current.satisfy(ce.getCondition())) {
                                nextState.apply(ce.getEffect());
                            }
                        }

                        // Create a new node, child to the current node, corresponding to the new state
                        Node next = new Node(nextState, current, current.getCost() + 1, a,
                                current.getDepth() + 1, current.getAgentFacts(),
                                current.getCarrierFacts(), current.getBoxFacts());

                        // If this node has not been already explored and its heuristic is less than
                        // Double.MAX_VALUE, then it can be considered insertable
                        if (!alreadyExploredNodes.contains(next) && heuristic.estimate(next) < Double.MAX_VALUE) {
                            successors.add(next);
                            insertableSuccessors++;
                            indexesOfInsertableSuccessors.add(insertableSuccessors - 1);
                        }
                    }
                }

                // If no successor is insertable (all of them has an heuristic equal to Double.MAX_VALUE), we proceed
                // like this: if the current node is the root, we add them all, because we are at the beginning of
                // the search; otherwise we add to the frontier only a part of them. The number of non-insertable
                // successors which is added becomes less while we go further down the search tree, because while in
                // the beginning we can cope with a non-acceptable state, further in the search it cannot happen any longer.
                if(insertableSuccessors == 0 && current.getDepth() == 0)
                    frontier.addAll(successors);

                else if(insertableSuccessors == 0 && current.getDepth() > 0 && successors.size()/(current.getDepth()) > 0)
                    frontier.addAll(successors.subList(0, successors.size()/(current.getDepth())));

                // If instead there are insertable successors, then we add them into the frontier, ignoring
                // those who are not insertable
                else if(insertableSuccessors > 0){
                    for(int index : indexesOfInsertableSuccessors)
                        frontier.add(successors.get(index));
                }

                // This sequence of prunings of the tree allow us to make the search quicker, without lose the
                // optimality of the solution.
            }
        }

        // If we arrive here, the search didn't find a solution, so we simply return null.
        this.exploredNodes = alreadyExploredNodes.size();
        memoryUsedForSearch += GraphLayout.parseInstance(frontier).totalSize();
        this.getStatistics().setMemoryUsedToSearch(memoryUsedForSearch);
        return null;
    }

    /**
     * Extract the plan from the solution node, going up the tree and adding the actions performed to get to
     * the solution.
     *
     * @param solution the solution node
     * @return a plan for the problem
     * */
    private Plan extractPlan(Node solution) {
        if (solution != null) {
            Node n = solution;
            SequentialPlan plan = new SequentialPlan();
            while (n.getAction() != null) {
                plan.add(0, n.getAction());
                n = n.getParent();
            }
            return plan;
        }
        else {
            return null;
        }
    }

    /**
     * Check if a problem is supported
     * */

    @Override
    public boolean isSupported(Problem problem) {
        return true;
    }


    public static void main(String[] args) {
        MyAlgorithm planner = new MyAlgorithm();
        planner.setTimeout(600);
        planner.setLogLevel(LogLevel.INFO);

        CommandLine cmd = new CommandLine(planner);
        cmd.execute(args);

        LOGGER.info(String.format("number of explored nodes:  %d\n\n", planner.getExploredNodes()));
    }

}