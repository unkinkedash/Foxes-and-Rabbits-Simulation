import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a field containing
 * rabbits and foxes.
 * 
 * @author David J. Barnes and Michael Kolling
 * @version 2002-04-09
 */
public class Simulator
{
    // The private static final variables represent 
    // configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 50;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 50;
    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.02;
    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.08;    
    // The probability that a rabbit will be created in any given grid position.
    private static final double HUNTER_CREATION_PROBABILITY = 0.09;

    // The list of animals in the field
    private List characters;
    // The list of animals just born
    private List newAnimals;
    // The current state of the field.
    private Field field;
    // A second field, used to build the next stage of the simulation.
    private Field updatedField;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        characters = new ArrayList();
        newAnimals = new ArrayList();
        field = new Field(depth, width);
        updatedField = new Field(depth, width);

        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Fox.class, Color.blue);
        view.setColor(Rabbit.class, Color.orange);
        view.setColor(Hunter.class, Color.black);
        
        // Setup a valid starting point.
        reset();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long period,
     * e.g. 500 steps.
     */
    public void runLongSimulation()
    {
        simulate(100);
    }
    
    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     */
    public void simulate(int numSteps)
    {
        for(int step = 1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * fox and rabbit.
     */
    public void simulateOneStep()
    {
        step++;
        newAnimals.clear();
        
        // let all animals act
        for(Iterator iter = characters.iterator(); iter.hasNext(); ) {
            Object character = iter.next();
            if(character instanceof Rabbit) {
                Rabbit rabbit = (Rabbit)character;
                if(rabbit.isAlive()) {
                    rabbit.run(updatedField, newAnimals);
                }
                else {
                    iter.remove();   // remove dead rabbits from collection
                }
            }
            else if(character instanceof Fox) {
                Fox fox = (Fox)character;
                if(fox.isAlive()) {
                    fox.hunt(field, updatedField, newAnimals);
                }
                else {
                    iter.remove();   // remove dead foxes from collection
                }
            } else if (character instanceof Hunter){
                Hunter hunter = (Hunter)character;
                if (hunter.isAlive()){
                    hunter.hunt(field,updatedField,newAnimals);
                } else {
                    iter.remove();
                }
            }
            else {
                System.out.println("found unknown animal");
            }
        }
        // add new born animals to the list of animals
        characters.addAll(newAnimals);
        
        // Swap the field and updatedField at the end of the step.
        Field temp = field;
        field = updatedField;
        updatedField = temp;
        updatedField.clear();

        // display the new field on screen
        view.showStatus(step, field);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        characters.clear();
        field.clear();
        updatedField.clear();
        populate(field);
        
        // Show the starting state in the view.
        view.showStatus(step, field);
    }
    
    /**
     * Populate the field with foxes and rabbits.
     */
    private void populate(Field field)
    {
        Random rand = new Random();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Fox fox = new Fox(true);
                    characters.add(fox);
                    fox.setLocation(row, col);
                    field.place(fox, row, col);
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Rabbit rabbit = new Rabbit(true);
                    characters.add(rabbit);
                    rabbit.setLocation(row, col);
                    field.place(rabbit, row, col);
                } else if (rand.nextDouble() <= HUNTER_CREATION_PROBABILITY) {
                    Hunter hunter = new Hunter(true);
                    characters.add(hunter);
                    hunter.setLocation(row, col);
                    field.place(hunter,row,col);
                }
                // else leave the location empty.
            }
        }
        Collections.shuffle(characters);
    }
}
