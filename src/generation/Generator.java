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
    private final double standDev;
    private final double repeatFactor;

    // String startRange - name of starting range for notes
    // String endRange - name of ending of range for notes.
    // String key - note name of major key that melody should be within.
    // int measures - number of measures for Generator to create.
    // double standDev - The standard deviation of base normal distribution. Higher standDev means bigger jumps. 1 is a good default.
    // double repeatFactor - number between 0 and 1 that describes how to modify repeated note. 1 is no modification, 0 means no chance of repeat.
    public Generator(String startRange, String endRange, String key, int measures, double repeatFactor){
        this.startRange = startRange;
        this.endRange = endRange;
        this.standDev = 1;
        this.repeatFactor = repeatFactor;
        this.numMeasures = measures;
        setKeySig(key);
        this.keyCenter = key;
        System.out.println("keyCenter = " + keyCenter);
        System.out.println("keySig = " + keySig);

        start();
        genPascalRow();
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

        while(!(Integer.toString(currOctave) + currNote).equals(endRange)){
            try{
                currNote = keySig.get(currIndex++);
            }
            catch(IndexOutOfBoundsException e){
                currIndex = 0;
                currNote = keySig.get(currIndex++);
                currOctave++;
            }
            allNotes.add(Integer.toString(currOctave) + currNote);
        }
        System.out.println(allNotes);

        for (int i = 0; i < allNotes.size(); i++) { //This sets the first note to the key center (tonic/the first note of the scale.))
            if(allNotes.get(i).substring(1).equals(keyCenter)){
                prevNote = allNotes.get(i);
                break;
            }
        }
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
            startRange = startRange.substring(0, 1) + keySig.get(startIndex);
            endRange = endRange.substring(0, 1) + keySig.get(endIndex);
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

        for (int val: pascalRow) {
            probabilities.add(0.0); // Adds values into probabilities array to use 'set' method later.
        }

        ArrayList<Integer> tempRow = new ArrayList<>();
        tempRow.addAll(pascalRow);
        int prevIndex = allNotes.indexOf(prevNote);
        System.out.println("prevIndex = " + prevIndex);
        double max = Collections.max(tempRow); //Determines max value in prob array to set rearrange around prev note.
        tempRow.remove(tempRow.indexOf((int)max));
        probabilities.set(prevIndex, max); //Sets the highest value, first to allow easier looping.

        for (int i = 1; i < probabilities.size(); i++) {
            if(prevIndex-i >= 0) {
                max = Collections.max(tempRow);
                tempRow.remove(tempRow.indexOf((int)max));
                probabilities.set(prevIndex - i, max);
            }

            if(prevIndex+i <= probabilities.size()-1) {
                max = Collections.max(tempRow);
                tempRow.remove(tempRow.indexOf((int)max));
                probabilities.set(prevIndex + i, max);
            }
        }
        adjustProb(prevIndex, repeatFactor);

        double total = getSum();
        for (double val : probabilities) {
            probabilities.set(probabilities.indexOf(val), val/total);
        }
        System.out.println("probabilities = " + probabilities);

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

    private void genPascalRow(){ //Generates row of pascals triangle with enough values for each note within given range.
        int amt = allNotes.size();
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

    public ArrayList<Note> getNotes(){
        ArrayList<Note> full = new ArrayList<>();
        for(Measure measure : melody){
            full.addAll(measure.getNotes());
        }
        return full;
    }

    public ArrayList<Measure> getMelody(){
        return melody;
    }
}