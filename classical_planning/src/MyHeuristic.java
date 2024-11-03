import fr.uga.pddl4j.problem.Fluent;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Condition;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import fr.uga.pddl4j.problem.operator.Effect;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.util.BitVector;
import java.util.*;

/**
 * Class that provides a method for computing the heuristic and memorizes a complete mapping of the object
 * of the problem, useful to determine the heuristic of a node. The heuristic is computed by verifying a series of
 * properties that a good plan should have: if at least one of these is not verified, the node is assigned a
 * heuristic value equal to <code>Double.MAX_VALUE</code>; otherwise it uses the value provided by the SUM
 * heuristic, which leverages a relaxed version of the graph that does not take into account the negative effects
 * of the actions: even if it isn't an admissible heuristic, this does not have any negative consequences on the
 * result of the search, on the contrary it's ideal to speed up operations since it's so easy to compute.
 * Other details on the SUM heuristic can be found in the PDDL4J library.
 * */
public class MyHeuristic {

    private Condition goal;

    private List<Fluent> facts;

    private List<Action> actions;

    private boolean isAdmissible;

    private int[][] unconditionalOperators;

    private int[] precondCardinality;

    private int[] operatorsLevel;

    private int[] precondCounters;

    private int[] operatorsDifficulty;

    private int[] pPropLevel;

    private int[] nPropLevel;

    private Condition[] precondEdges;

    private Condition[] effectsEdges;

    private Condition[] preconditions;

    private Effect[] effects;

    private Effect[] unconditionalEffects;

    private int goalCounter;

    private int goalCardinality;

    private int level;

    private Problem problem;

    /**
     * Mapping between the type of the objects and the objects actually involved in the problem.
     * The objects are identified by an integer value that corresponds to the order by which they are declared
     * in the field <code>(:objects)</code> of the .pddl file that contains the problem. So that order is kept in
     * this mapping, thanks to which it's possible to access information like the number of object of a certain type
     * and the object associated to a certain identifier.
     * */
    private HashMap<String, TreeSet<Integer>> mapping;

    public MyHeuristic(Problem problem) {

        // The problem to solve
        this.problem = problem;

        // The fluents, i.e. the predicates that change value during the search
        this.facts = problem.getFluents();

        // The goal to achieve
        this.goal = problem.getGoal();

        // The actions of the problem
        this.actions = problem.getActions();

        // The mapping <object_type, list_of_object_of_that_type>
        this.mapping = new HashMap<>();

        // For each action of the problem, we take the involved objects and insert them into the mapping.
        // Each action has an order by which the parameters are passed: following that order for each action,
        // it's possible to collect every object of a certain type involved in the problem. Thanks to the mapping,
        // it's possible to access information like the number of objects of a certain type and the object
        // associated to a certain identifier.
        for(Action ac : this.actions){

            // Take the identifiers of the objects involved in the action
            int[] instantiations = ac.getInstantiations();

            // Register these objects inside the mapping according to the type of the action
            switch (ac.getName().toLowerCase()){
                case "move_agent":
                    if(!mapping.containsKey("agent"))
                        mapping.put("agent", new TreeSet<>());

                    mapping.get("agent").add(instantiations[0]);

                    if(!mapping.containsKey("location"))
                        mapping.put("location", new TreeSet<>());

                    mapping.get("location").add(instantiations[1]);
                    mapping.get("location").add(instantiations[2]);

                    break;

                case "move_agent_and_carrier":
                    if(!mapping.containsKey("agent"))
                        mapping.put("agent", new TreeSet<>());

                    mapping.get("agent").add(instantiations[0]);

                    if(!mapping.containsKey("carrier"))
                        mapping.put("carrier", new TreeSet<>());

                    mapping.get("carrier").add(instantiations[1]);

                    if(!mapping.containsKey("location"))
                        mapping.put("location", new TreeSet<>());

                    mapping.get("location").add(instantiations[2]);
                    mapping.get("location").add(instantiations[3]);

                    break;

                case "fill_box_and_load_it_on_carrier":
                    if(!mapping.containsKey("agent"))
                        mapping.put("agent", new TreeSet<>());

                    mapping.get("agent").add(instantiations[0]);

                    if(!mapping.containsKey("carrier"))
                        mapping.put("carrier", new TreeSet<>());

                    mapping.get("carrier").add(instantiations[1]);

                    if(!mapping.containsKey("box"))
                        mapping.put("box", new TreeSet<>());

                    mapping.get("box").add(instantiations[2]);

                    if(!mapping.containsKey("box_place"))
                        mapping.put("box_place", new TreeSet<>());

                    mapping.get("box_place").add(instantiations[3]);

                    if(!mapping.containsKey("content"))
                        mapping.put("content", new TreeSet<>());

                    mapping.get("content").add(instantiations[4]);

                    break;

                case "unload_box_deliver_its_content_and_reload_it_on_carrier":
                    if(!mapping.containsKey("agent"))
                        mapping.put("agent", new TreeSet<>());

                    mapping.get("agent").add(instantiations[0]);

                    if(!mapping.containsKey("carrier"))
                        mapping.put("carrier", new TreeSet<>());

                    mapping.get("carrier").add(instantiations[1]);

                    if(!mapping.containsKey("box"))
                        mapping.put("box", new TreeSet<>());

                    mapping.get("box").add(instantiations[2]);

                    if(!mapping.containsKey("content"))
                        mapping.put("content", new TreeSet<>());

                    mapping.get("content").add(instantiations[3]);

                    if(!mapping.containsKey("person"))
                        mapping.put("person", new TreeSet<>());

                    mapping.get("person").add(instantiations[4]);

                    if(!mapping.containsKey("location"))
                        mapping.put("location", new TreeSet<>());

                    mapping.get("location").add(instantiations[5]);

                    break;

                case "unload_empty_box_from_carrier":
                    if(!mapping.containsKey("agent"))
                        mapping.put("agent", new TreeSet<>());

                    mapping.get("agent").add(instantiations[0]);

                    if(!mapping.containsKey("carrier"))
                        mapping.put("carrier", new TreeSet<>());

                    mapping.get("carrier").add(instantiations[1]);

                    if(!mapping.containsKey("box"))
                        mapping.put("box", new TreeSet<>());

                    mapping.get("box").add(instantiations[2]);

                    if(!mapping.containsKey("box_place"))
                        mapping.put("box_place", new TreeSet<>());

                    mapping.get("box_place").add(instantiations[3]);
                    break;
            }
        }

        final int nbRelevantFacts = facts.size();

        final int nbOperators = getActions().size();

        int nbUncondOperators = 0;

        final List<Action> operators = problem.getActions();
        for (Action op : operators) {
            nbUncondOperators += op.getConditionalEffects().size();
        }

        this.pPropLevel = new int[nbRelevantFacts];

        this.nPropLevel = new int[nbRelevantFacts];

        this.operatorsLevel = new int[nbUncondOperators];

        this.operatorsDifficulty = new int[nbUncondOperators];

        this.precondCounters = new int[nbUncondOperators];

        this.preconditions = new Condition[nbUncondOperators];

        this.effects = new Effect[nbUncondOperators];

        this.unconditionalEffects = new Effect[nbOperators];
        for (int i = 0; i < this.unconditionalEffects.length; i++) {
            this.unconditionalEffects[i] = new Effect();
        }

        this.unconditionalOperators = new int[nbUncondOperators][];

        this.precondEdges = new Condition[nbRelevantFacts];
        for (int i = 0; i < this.precondEdges.length; i++) {
            this.precondEdges[i] = new Condition();
        }

        this.effectsEdges = new Condition[nbRelevantFacts];
        for (int i = 0; i < this.effectsEdges.length; i++) {
            this.effectsEdges[i] = new Condition();
        }

        this.goalCardinality = goal.cardinality();

        this.precondCardinality = new int[nbUncondOperators];

        int uncondOpIndex = 0;

        for (int opIndex = 0; opIndex < operators.size(); opIndex++) {
            final Action op = operators.get(opIndex);
            final List<ConditionalEffect> condEffects = op.getConditionalEffects();

            for (int ceIndex = 0; ceIndex < condEffects.size(); ceIndex++) {
                final ConditionalEffect cEffect = condEffects.get(ceIndex);
                final int[] eff = {opIndex, ceIndex};
                this.unconditionalOperators[uncondOpIndex] = eff;

                final Condition pre = new Condition(op.getPrecondition());
                final BitVector pPre = pre.getPositiveFluents();
                final BitVector nPre = pre.getNegativeFluents();
                pPre.or(cEffect.getCondition().getPositiveFluents());
                nPre.or(cEffect.getCondition().getNegativeFluents());
                for (int p = pPre.nextSetBit(0); p >= 0; p = pPre.nextSetBit(p + 1)) {
                    this.precondEdges[p].getPositiveFluents().set(uncondOpIndex);
                }
                for (int p = nPre.nextSetBit(0); p >= 0; p = nPre.nextSetBit(p + 1)) {
                    this.precondEdges[p].getNegativeFluents().set(uncondOpIndex);
                }

                this.preconditions[uncondOpIndex] = pre;

                final Effect effect = cEffect.getEffect();
                final BitVector pEff = effect.getPositiveFluents();
                final BitVector nEff = effect.getNegativeFluents();
                for (int p = pEff.nextSetBit(0); p >= 0; p = pEff.nextSetBit(p + 1)) {
                    this.effectsEdges[p].getPositiveFluents().set(uncondOpIndex);
                }
                for (int p = nEff.nextSetBit(0); p >= 0; p = nEff.nextSetBit(p + 1)) {
                    this.effectsEdges[p].getNegativeFluents().set(uncondOpIndex);
                }

                this.effects[uncondOpIndex] = effect;

                this.precondCardinality[uncondOpIndex] = pre.cardinality();

                if (cEffect.getCondition().isEmpty()) {
                    final Effect uncondEff = this.unconditionalEffects[opIndex];
                    final Effect condEff = cEffect.getEffect();
                    uncondEff.getPositiveFluents().or(condEff.getPositiveFluents());
                    uncondEff.getNegativeFluents().or(condEff.getNegativeFluents());
                }

                uncondOpIndex++;
            }
        }

        for (int i = 0; i < nbUncondOperators; i++) {
            if (this.preconditions[i].isEmpty()) {
                for (Condition pEdge : precondEdges) {
                    pEdge.getPositiveFluents().set(i);
                    pEdge.getNegativeFluents().set(i);
                }
            }
        }
    }

    public int getNumAgents(){
        return mapping.get("agent").size();
    }

    public int getAgentAtIndex(int i){
        Integer[] agents = new Integer[getNumAgents()];
        return mapping.get("agent").toArray(agents)[i];
    }

    public int getNumCarriers(){
        return mapping.get("carrier").size();
    }

    public int getCarrierAtIndex(int i){
        Integer[] carriers = new Integer[getNumCarriers()];
        return mapping.get("carrier").toArray(carriers)[i];
    }

    public int getNumBoxes(){
        return mapping.get("box").size();
    }

    public int getBoxAtIndex(int i){
        Integer[] boxes = new Integer[getNumBoxes()];
        return mapping.get("box").toArray(boxes)[i];
    }

    public int getNumBoxPlaces(){
        return mapping.get("box_place").size();
    }

    public int getBoxPlaceAtIndex(int i){
        Integer[] box_places = new Integer[getNumBoxPlaces()];
        return mapping.get("box_place").toArray(box_places)[i];
    }

    public int getNumLocations(){
        return mapping.get("location").size();
    }

    public int getLocationAtIndex(int i){
        Integer[] locations = new Integer[getNumLocations()];
        return mapping.get("location").toArray(locations)[i];
    }

    public int getNumContents(){
        return mapping.get("content").size();
    }

    public int getContentAtIndex(int i){
        Integer[] contents = new Integer[getNumContents()];
        return mapping.get("content").toArray(contents)[i];
    }

    public int getNumPeople(){
        return mapping.get("person").size();
    }

    public int getPersonAtIndex(int i){
        Integer[] people = new Integer[getNumPeople()];
        return mapping.get("person").toArray(people)[i];
    }

    public String getTypeOfParameter(int x){
        for(String type: mapping.keySet())
            if(mapping.get(type).contains(x))
                return type;
        return null;
    }


    public boolean isAdmissible() {
        return this.isAdmissible;
    }

    private void setAdmissible(boolean isAdmissible) {
        this.isAdmissible = isAdmissible;
    }

    private List<Action> getActions() {
        return this.actions;
    }

    private int expandRelaxedPlanningGraph(State state) {

        Arrays.fill(this.operatorsLevel, Integer.MAX_VALUE);

        Arrays.fill(this.pPropLevel, Integer.MAX_VALUE);

        Arrays.fill(this.nPropLevel, Integer.MAX_VALUE);

        Arrays.fill(this.precondCounters, 0);

        Arrays.fill(this.operatorsDifficulty, Integer.MAX_VALUE);

        final BitVector pGoal = goal.getPositiveFluents();

        final BitVector nGoal = goal.getNegativeFluents();

        this.goalCounter = 0;

        this.level = 0;

        BitVector ppk = new BitVector(state);

        BitVector npk = new BitVector();
        npk.flip(0, facts.size());
        npk.andNot(state);

        for (int p = ppk.nextSetBit(0); p >= 0; p = ppk.nextSetBit(p + 1)) {
            this.pPropLevel[p] = 0;
            if (pGoal.get(p)) {
                this.goalCounter++;
            }
        }

        for (int p = npk.nextSetBit(0); p >= 0; p = npk.nextSetBit(p + 1)) {
            this.nPropLevel[p] = 0;
            if (nGoal.get(p)) {
                this.goalCounter++;
            }
        }

        final BitVector pAcc = new BitVector();

        final BitVector nAcc = new BitVector();

        while (this.goalCounter != this.goalCardinality && (!ppk.isEmpty() || !npk.isEmpty())) {

            final BitVector newOps = new BitVector();

            for (int p = ppk.nextSetBit(0); p >= 0; p = ppk.nextSetBit(p + 1)) {

                final BitVector pEdges = this.precondEdges[p].getPositiveFluents();

                pAcc.set(p);

                for (int pe = pEdges.nextSetBit(0); pe >= 0; pe = pEdges.nextSetBit(pe + 1)) {

                    if (this.precondCardinality[pe] != 0) {
                        this.precondCounters[pe]++;
                    }

                    if (this.precondCounters[pe] == this.precondCardinality[pe]) {
                        newOps.set(pe);
                    }
                }
            }

            for (int p = npk.nextSetBit(0); p >= 0; p = npk.nextSetBit(p + 1)) {

                final BitVector nEdges = this.precondEdges[p].getNegativeFluents();

                nAcc.set(p);

                for (int pe = nEdges.nextSetBit(0); pe >= 0; pe = nEdges.nextSetBit(pe + 1)) {

                    if (this.precondCardinality[pe] != 0) {
                        this.precondCounters[pe]++;
                    }

                    if (this.precondCounters[pe] == this.precondCardinality[pe]) {
                        newOps.set(pe);
                    }
                }
            }

            final BitVector pNewProps = new BitVector();

            final BitVector nNewProps = new BitVector();

            for (int o = newOps.nextSetBit(0); o >= 0; o = newOps.nextSetBit(o + 1)) {

                this.operatorsLevel[o] = this.level;

                pNewProps.or(this.effects[o].getPositiveFluents());

                nNewProps.or(this.effects[o].getNegativeFluents());

                this.operatorsDifficulty[o] = 0;

                final BitVector pPre = this.preconditions[o].getPositiveFluents();
                for (int p = pPre.nextSetBit(0); p >= 0; p = pPre.nextSetBit(p + 1)) {
                    this.operatorsDifficulty[o] += this.pPropLevel[p];
                }

                final BitVector nPre = this.preconditions[o].getNegativeFluents();
                for (int p = nPre.nextSetBit(0); p >= 0; p = nPre.nextSetBit(p + 1)) {
                    this.operatorsDifficulty[o] += this.nPropLevel[p];
                }
            }

            ppk = pNewProps;
            npk = nNewProps;
            ppk.andNot(pAcc);
            npk.andNot(nAcc);

            this.level++;

            for (int p = ppk.nextSetBit(0); p >= 0; p = ppk.nextSetBit(p + 1)) {
                this.pPropLevel[p] = this.level;

                if (pGoal.get(p)) {
                    this.goalCounter++;
                }
            }

            for (int p = npk.nextSetBit(0); p >= 0; p = npk.nextSetBit(p + 1)) {
                this.nPropLevel[p] = this.level;

                if (nGoal.get(p)) {
                    this.goalCounter++;
                }
            }
        }
        return this.level;
    }

    private boolean isGoalReachable() {
        return this.goalCardinality == this.goalCounter;
    }

    /**
     * Determines the SUM value, used while computing the heuristic
     * */
    private int getSumValue() {
        int value = 0;
        final BitVector pGoal = goal.getPositiveFluents();
        final BitVector nGoal = goal.getNegativeFluents();
        for (int g = pGoal.nextSetBit(0); g >= 0; g = pGoal.nextSetBit(g + 1)) {
            value += this.pPropLevel[g];
        }
        for (int g = nGoal.nextSetBit(0); g >= 0; g = nGoal.nextSetBit(g + 1)) {
            value += this.nPropLevel[g];
        }
        return value;
    }

    /**
     * Method that provides the heuristic associated with the <code>node</code>
     * */
    public double estimate(Node node){
        // The heuristic we're using is not admissible
        setAdmissible(false);

        // We expand the relaxed graph based on the state represented by the current node
        expandRelaxedPlanningGraph(node);

        // If it's not possible to reach the goal from this node, we return Double.MAX_VALUE
        if(!isGoalReachable())
            return Double.MAX_VALUE;

        // We take the minimum between the number of boxes and the number of box_place. If the depth of the node
        // is less or equal than this minimum (so we are in the first actions) and the number of loaded boxes
        // does not correspond to the depth of the node (so actions different from "fill_box_and_load_it_on_carrier"
        // have been performed) we return Double.MAX_VALUE
        int numLoadedBoxes = node.getNumLoadedBoxes(); // Number of loaded boxes
        int numBoxes = getNumBoxes();                  // Total number of boxes
        int numBoxPlaces = getNumBoxPlaces();          // Total number of box_place

        if(node.getDepth() <= Integer.min(numBoxes, numBoxPlaces) && numLoadedBoxes != node.getDepth())
            return Double.MAX_VALUE;

        // After the "fill_box_and_load_it_on_carrier" actions, there must be a number of moves equal to the
        // minimum between the number of agents and the number of carriers
        int numMoveActions = node.getTotalNumberOfMoveActions();
        int numAgents = getNumAgents();
        int numCarriers = getNumCarriers();

        if(node.getDepth() == Integer.min(numBoxes, numBoxPlaces) + Integer.min(numAgents, numCarriers)
                && numMoveActions != Integer.min(numAgents, numCarriers))
            return Double.MAX_VALUE;

        // If an agent performs a move action, the action which immediately follows this one must not be another move,
        // otherwise it's a useless action
        if(tooMuchMoves(node))
            return Double.MAX_VALUE;

        // If the node passes these controls, then gets as heuristic value the one provided by the SUM heuristic.
        // If the node is a solution, SUM returns 0
        int sumValue = getSumValue();
        return sumValue;
    }

    private boolean tooMuchMoves(Node node){
        if(node.getAction() != null){
            Action action1 = node.getAction();
            int agent1 = action1.getInstantiations()[0];
            if(action1.getName().equalsIgnoreCase("move_agent") || action1.getName().equalsIgnoreCase("move_agent_and_carrier")){
                node = node.getParent();
                while(node != null && node.getAction() != null){
                    Action action2 = node.getAction();
                    int agent2 = action2.getInstantiations()[0];
                    if(agent1 == agent2){
                        if(action2.getName().equalsIgnoreCase("move_agent") || action2.getName().equalsIgnoreCase("move_agent_and_carrier"))
                            return true;
                        else
                            return false;
                    }
                    else{
                        node = node.getParent();
                    }
                }
            }
            else{
                return false;
            }
        }
        return false;
    }
}
