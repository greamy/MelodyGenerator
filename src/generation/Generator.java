package generation;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Generator {
    private String startRange;
    private String endRange;
    private final String keyCenter;

    private final ArrayList<String> keySig = new ArrayList<>(Arrays.asList("A", "B", "C", "D", "E", "F", "G"));
    private final ArrayList<String> allNotes = new ArrayList<>();
    public final ArrayList<Double> probabilities = new ArrayList<>();
    private final ArrayList<Measure> melody = new ArrayList<>();
    private final ArrayList<Integer> pascalRow = new ArrayList<>();
    private String prevNote;

    private final int numMeasures;
    private final double repeatFactor;

    private final ArrayList<Integer> possibleResolutions = new ArrayList<>();
    private int sinceResolve = 0; //# of notes since last resolution.
    private double resolveFactor = 0.1; //This increases the longer there is no resolution.
    private int resolving = 0; //0 means not resolving, 1 means resolving down, -1 is resolving upward.
    private int resolveCt = 0;
    private int closestResolve = 0; //Index for the closest note to resolve to, at time of successful resolution check.

    // String startRange - name of starting range for notes
    // String endRange - name of ending of range for notes.
    // String key - note name of major key that melody should be within.
    // int measures - number of measures for Generator to create.
    // double repeatFactor - number between 0 and 1 that describes how to modify repeated note. 1 is no modification, 0 means no chance of repeat.
    public Generator(String startRange, String endRange, String key, int measures, double repeatFactor){
        this.startRange = startRange;
        this.endRange = endRange;
        this.repeatFactor = repeatFactor;
        this.numMeasures = measures;
        setKeySig(key);
        this.keyCenter = key;
        System.out.println("keyCenter = " + keyCenter);
        System.out.println("keySig = " + keySig);

        start();
        genPascalRow(allNotes.size());
        System.out.println("pascalRow = " + pascalRow);
        System.out.println("startNote = " + prevNote);
    }

    // Defines allNotes arrayList by looping through possible notes from start range to end range.
    private void start(){
        String currNote = startRange.substring(1); //Grabs the note name and converts it to char.
        int currOctave = Integer.parseInt(startRange.substring(0, 1)); //Grabs the octave before the note name.
        if(currOctave > Integer.parseInt(endRange.substring(0,1))){
            System.out.println("Invalid range input. Please make sure first note is lower than the second.");
            throw new InvalidParameterException("Invalid range input. Please make sure first note is lower than the second.");
        }

        int currIndex = keySig.indexOf(currNote);

        while(!(currOctave + currNote).equals(endRange)){
            try{
                currNote = keySig.get(currIndex++);
            }
            catch(IndexOutOfBoundsException e){
                currIndex = 0;
                currNote = keySig.get(currIndex++);
                currOctave++;
            }
            allNotes.add(currOctave + currNote);
        }
        System.out.println(allNotes);

        for (int i = 0; i < allNotes.size(); i++) { //This sets the first note to the key center (tonic/the first note of the scale.))
            if(allNotes.get(i).substring(1).equals(keyCenter)){
                possibleResolutions.add(i);
            }
        }
        //Gets a random note from possible starting points (all notes within range that are the root of scale).
        prevNote = allNotes.get((int)(Math.random() * (possibleResolutions.size())));
    }

    // Sets key signature for use in creating list of all possible notes.
    private void setKeySig(String key){
        int startIndex = keySig.indexOf(startRange.substring(1));
        int endIndex = keySig.indexOf(endRange.substring(1));
        switch (key){
            case "Gb":
                keySig.set(2, "Cb");
            case "Db":
                keySig.set(6, "Gb");
            case "Ab":
                keySig.set(3, "Db");
            case "Eb":
                keySig.set(0, "Ab");
            case "Bb":
                keySig.set(4, "Eb");
            case "F":
                keySig.set(1, "Bb");
                break;
            case "F#":
                keySig.set(5, "E#");
            case "B":
                keySig.set(0, "A#");
            case "E":
                keySig.set(3, "D#");
            case "A":
                keySig.set(6, "G#");
            case "D":
                keySig.set(3, "C#");
            case "G":
                keySig.set(5, "F#");
                break;
            case "C":
                break;
            default:
                throw new InvalidParameterException("Invalid Key input. Please enter a a major key, with 'b' for flat, and '#' for sharp.");
        }

        //This logic essentially ensures start and end ranges are within the key entered.
        if(startIndex != -1 && endIndex != -1) {
            startRange = startRange.charAt(0) + keySig.get(startIndex);
            endRange = endRange.charAt(0) + keySig.get(endIndex);
        }
        else{
            startIndex = keySig.indexOf(startRange.substring(1));
            endIndex = keySig.indexOf(endRange.substring(1));
            if(startIndex == -1 || endIndex == -1){
                throw new InvalidParameterException("Invalid Range input. Please input like [octave][noteName][#/b]");
            }
        }

    }

    // Generates number of measures needed.
    public void generate(){
        for(int i = 0; i < numMeasures; i++){
            Measure meas = new Measure();
            for (int j = 0; j < meas.getNumNotes(); j++) {
                meas.setNote(j, genNote());
            }
            melody.add(meas);
        }
    }

    // Updates probabilities as described below, then runs the random num generation to choose a note.
    private String genNote() {
        String resolve = checkResolve();
        if(resolve != null) return resolve;

        double rest = Math.random();
        if (rest <= 0.05) return "R";
        pascalProbs();
        double choice = Math.random();
        double sum = 0;
        for (int r = 0; r < probabilities.size(); r++) {
            sum += probabilities.get(r);
            if (choice <= sum) {
                prevNote = allNotes.get(r);
                return prevNote;
            }
        }
        return null;
    }

    // Helper method to ensure probabilities add to 1.0
    public double getSum(){
        double sum = 0;
        for (double probability : probabilities) {
            sum += probability;
        }
        return sum;
    }

    public void pascalProbs(){
        probabilities.clear();    //Setup variables and function calls.

        for (int ignored : pascalRow) {
            probabilities.add(0.0); // Adds values into probabilities array to use 'set' method later.
        }

        ArrayList<Integer> tempRow = new ArrayList<>(pascalRow);
        int prevIndex = allNotes.indexOf(prevNote);
        double max = Collections.max(tempRow); //Determines max value in prob array to set rearrange around prev note.
        tempRow.remove((Integer) (int) max);
        probabilities.set(prevIndex, max); //Sets the highest value, first to allow easier looping.

        for (int i = 1; i < probabilities.size(); i++) {
            if(prevIndex-i >= 0) {
                max = Collections.max(tempRow);
                tempRow.remove((Integer) (int) max);
                probabilities.set(prevIndex - i, max);
            }

            if(prevIndex+i <= probabilities.size()-1) {
                max = Collections.max(tempRow);
                tempRow.remove((Integer) (int) max);
                probabilities.set(prevIndex + i, max);
            }
        }
        adjustProb(prevIndex, repeatFactor);
        for (int index : possibleResolutions) {
            adjustProb(index, resolveFactor);
        }

        double total = getSum();
        for (double val : probabilities) {
            probabilities.set(probabilities.indexOf(val), val/total);
        }
//        System.out.println("probabilities = " + probabilities);

        if(getSum() <= 0.985){
            System.out.println("Probability generation failed, getSum() != 1.");
        }
    }

    private long fact(int n){
        long sum = n;
        if(n==0){
            return 1;
        }
        for (int j = n-1; j > 0; j--) {
            sum *= j;
        }
        return sum;
    }

    private void genPascalRow(int amt){ //Generates row of pascals triangle with enough values for each note within given range.
        int row = amt-1;
        
        for (int i = 0; i < amt; i++) { //Gets an array with values equal to a row of the pascal triangle.
            int result = (int)(fact(row)/(fact(i)*fact(row-i)));
            pascalRow.add(result); // Essentially 'amt choose i', using factorials to perform choose function
        }
    }

    //Reduces chance of repeat note (Otherwise it would be the most common thing to happen).
    private void adjustProb(int index, double factor){
        double max = probabilities.get(index);
        double newMax = max * factor;
        probabilities.set(index, newMax);
    }

    //Performs checks for resolution, and updates probabilities for resolution.
    private String checkResolve(){
        if(resolving != 0){ //If in resolution, keep 'sinceResolve' at 0.
            sinceResolve = 0;
            resolveFactor = 0.25;
        } else{ //If not in resolution
            sinceResolve++;
        }
        resolveFactor += sinceResolve/10.0;

        if(resolving == 0) { //0 means it is not currently resolving, so it performs random roll for a resolution.
            resolveCt = 0;
            double resolve = Math.random();
            if (resolve <= 0.05 * resolveFactor) { //If this succeeds, a resolution has started.
                int prevIndex = allNotes.indexOf(prevNote);
                closestResolve = findClosestResolve(prevIndex); //Finds closest tonic to resolve to, given previous note.
                if((prevIndex - closestResolve) >= 0){
                    System.out.println("Resolving down");
                    resolving = 1; //Prev note is above closest resolution note, so we go resolve down to it.
                }
                else if((prevIndex - closestResolve) < 0){
                    System.out.println("Resolving up!");
                    resolving = -1; //Prev note is below closest resolution note, so we resolve upwards.
                }
            }
        }
        else { //This is if we are currently in a resolution (one resolution has 3 notes, either 6 7 8, or 3 2 1.
            resolveCt++; //Tells what step of resolution we are on
            switch(resolveCt) {
                case 1: //This is the first note of resolution. multiplied by resolving because it will either be 1 or -1/
                    return allNotes.get(closestResolve+(2*resolving));
                case 2: // Second note of resolution.
                    return allNotes.get(closestResolve+(1*resolving));
                case 3: //3rd and final note, we are done with resolution, and will resume normal melody next note.
                    resolving = 0;
                    return allNotes.get(closestResolve);
            }
        }
        return null; //If not resolving, return null so genNote() can continue as normal
    }

    private int findClosestResolve(int prevIndex){
        int closest = 9999999;
        int index = 0;
        for (int val : possibleResolutions) {
            int dist = Math.abs(prevIndex - val);
            if (dist <= closest){
                closest = dist;
                index = val;
            }
        }
        System.out.println("index = " + index);
        return index;
    }

    public ArrayList<Note> getNotes(){
        ArrayList<Note> full = new ArrayList<>();
        for(Measure measure : melody){
            full.addAll(measure.getNotes());
        }
        return full;
    }
}