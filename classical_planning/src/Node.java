import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;

import java.util.HashMap;

public class Node extends State {
    /**
     * The parent node
     * */
    private Node parent;

    /**
     * The cost of the node
     * */
    private double cost;

    /**
     * The depth of the node
     * */
    private int depth;

    /**
     * The last action that brought to this node. If it's null, the current node is the root
     * */
    private Action action;

    /**
     * A matrix that stores, for each agent, the actions he performed
     * */
    private int[][] agentFacts;
    private final static int agent_ID = 0;
    private final static int move_agent = 1;
    private final static int move_agent_and_carrier = 2;
    private final static int fill_box_and_load_it_on_carrier = 3;
    private final static int unload_box_deliver_its_content_and_reload_it_on_carrier = 4;
    private final static int unload_empty_box_from_carrier = 5;

    /**
     * A matrix that stores, for each carrier, the number of boxes loaded right now and the number of actions
     * performed until now
     * */
    private int[][] carrierFacts;

    /**
     * Mapping between the boxes and their content
     * */
    private HashMap<Integer, Integer> boxFacts;


    public Node(State state, Node parent, double cost, Action action, int depth, int[][] agentFacts, int[][] carrierFacts, HashMap<Integer, Integer> boxFacts) {
        super(state);
        this.parent = parent;
        this.cost = cost;
        this.action = action;
        this.agentFacts = agentFacts;
        this.carrierFacts = carrierFacts;
        this.boxFacts = boxFacts;
        this.depth = depth;
        if(this.action != null) {
            // SET THE INFORMATION ON THE AGENT

            // Take the action parameters
            int[] actionParameters = this.action.getInstantiations();

            // The agent is always the parameter in the first position (0), regardless of the action
            int agent_ID = actionParameters[0];

            // Increment the number of actions of that type performed by that agent
            for (int i = 0; i < this.agentFacts.length; i++)
                if (this.agentFacts[i][0] == agent_ID)
                    this.agentFacts[i][getActionIndex(this.action.getName())]++;

            // SET THE INFORMATION THE CARRIER

            // The carrier is always the parameter in the second position (1), regardless of the action
            int carrier_ID = actionParameters[1];

            int op;
            switch (this.action.getName().toLowerCase()) {
                // If the action is this, we ought to increment the number of boxes on that carrier
                case "fill_box_and_load_it_on_carrier":
                    op = 1;
                    break;

                // If the action is this, we ought to decrement the number of boxes on that carrier
                case "unload_empty_box_from_carrier":
                    op = -1;
                    break;

                // Otherwise we don't do anything
                default:
                    op = 0;
                    break;
            }

            // Modify the number of boxes on the carrier
            for (int i = 0; i < this.carrierFacts.length; i++)
                if (this.carrierFacts[i][0] == carrier_ID)
                    this.carrierFacts[i][1] += op;


            // SET THE INFORMATION ON THE BOXES
            int box;
            int content;
            switch(this.action.getName().toLowerCase()){
                // If the action is this, we ought to add an entry: <box, content>
                case "fill_box_and_load_it_on_carrier":
                    box = actionParameters[2];      // In this action the box is the parameter in third position (2)
                    content = actionParameters[4];  // In this action the content is the parameter in fifth position (4)

                    // Add the entry
                    boxFacts.put(box, content);
                    break;

                case "unload_box_deliver_its_content_and_reload_it_on_carrier":
                    // Take the box and the content and delete the entry
                    box = actionParameters[2];      // In this action the box is the parameter in third position (2)
                    content = actionParameters[3];  // In this action the content is the parameter in fourth position (3)

                    // Delete the entry
                    boxFacts.remove(box, content);
                    break;
            }
        }
    }

    /**
     * Given the name of an action, returns the corresponding column index in the agentFacts matrix
     * */
    private int getActionIndex(String actionName){
        switch (actionName.toLowerCase()){
            case "move_agent":
                return move_agent;
            case "move_agent_and_carrier":
                return move_agent_and_carrier;
            case "fill_box_and_load_it_on_carrier":
                return fill_box_and_load_it_on_carrier;
            case "unload_box_deliver_its_content_and_reload_it_on_carrier":
                return unload_box_deliver_its_content_and_reload_it_on_carrier;
            case "unload_empty_box_from_carrier":
                return unload_empty_box_from_carrier;
            default: return -1;
        }
    }

    /**
     * Restituisce la matrice delle informazioni sugli agenti
     * */
    public int[][] getAgentFacts(){
        int[][] m = new int[this.agentFacts.length][this.agentFacts[0].length];
        for(int i=0;i<m.length;i++)
            for(int j=0;j<m[0].length;j++)
                m[i][j] = this.agentFacts[i][j];
        return m;
    }

    /**
     * Returns the matrix of the information on the carriers
     * */
    public int[][] getCarrierFacts(){
        int[][] m = new int[this.carrierFacts.length][this.carrierFacts[0].length];
        for(int i=0;i<m.length;i++)
            for(int j=0;j<m[0].length;j++)
                m[i][j] = this.carrierFacts[i][j];
        return m;
    }

    /**
     * Returns the mapping box-content
     * */
    public HashMap<Integer, Integer> getBoxFacts(){
        HashMap<Integer, Integer> ret = new HashMap<>();
        for(int key : boxFacts.keySet())
            ret.put(key, boxFacts.get(key));
        return ret;
    }

    /**
     * Returns the number of loaded boxes
     * */
    public int getNumLoadedBoxes(){
        int n = 0;
        for(int i=0;i<carrierFacts.length;i++)
            n += carrierFacts[i][1];
        return n;
    }

    /**
     * Returns the number of 'move_agent' and 'move_agent_and_carrier' actions performed until now
     * Restituisce il numero di azioni move_agent e move_agent_and_carrier effettuate finora
     * */
    public int getTotalNumberOfMoveActions(){
        int n = 0;
        for(int i=0;i<agentFacts.length;i++)
            n += agentFacts[i][move_agent] + agentFacts[i][move_agent_and_carrier];
        return n;
    }

    /**
     * Returns the number of 'fill_box_and_load_it_on_carrier' actions performed until now
     * */
    public int getTotalNumberOfFillActions(){
        int n = 0;
        for(int i=0;i<agentFacts.length;i++)
            n += agentFacts[i][fill_box_and_load_it_on_carrier];
        return n;
    }

    /**
     * Returns the number of 'unload_box_deliver_its_content_and_load_it_on_carrier' actions performed until now
     * */
    public int getTotalNumberOfDeliverActions(){
        int n = 0;
        for(int i=0;i<agentFacts.length;i++)
            n += agentFacts[i][unload_box_deliver_its_content_and_reload_it_on_carrier];
        return n;
    }

    /**
     * Returns the parent node
     * */
    public Node getParent() {
        return parent;
    }

    /**
     * Returns the total cost of this node
     * */
    public double getCost() {
        return cost;
    }

    /**
     * Returns the last action performed to get to this node
     * */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the depth of this node
     * */
    public int getDepth(){
        return depth;
    }
}
