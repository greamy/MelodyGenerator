package generation;

import java.util.ArrayList;

public class Measure {
    private ArrayList<Double> noteLengths = new ArrayList<>();
    private ArrayList<Note> notes = new ArrayList<>();
    private int[] timeSig = {4, 4};
    private int numNotes;

    public Measure(){
        noteLengths.add((double) timeSig[0]);
        splitCheck(new int[]{0}, 0.95);
        System.out.println("noteLengths = " + noteLengths);
        this.numNotes = noteLengths.size();
        for (int i = 0; i < numNotes; i++) {
            Note note = new Note("4C", noteLengths.get(i));
            notes.add(note);
        }
    }
    public Measure(int[] timeSig){
        this(); // Calls default constructor.
        this.timeSig = timeSig;
    }

    private void splitCheck(int[] index, double chance){
//        System.out.println("noteLengths = " + noteLengths);
//        System.out.print("Indexes: ");
//        for (int id: index) {
//            System.out.print(Integer.toString(id) + " ");
//        }
//        System.out.println();
        ArrayList<Integer> again = new ArrayList<>();
        for (int id : index) {
            double choice = Math.random();
            if (choice <= chance) {
                int currIndex = id + again.size();
                double temp = noteLengths.get(currIndex) / 2;
//                System.out.println("I just hit a " + chance*100.0 + "% chance!\nI created two " + temp + " duration notes\n");
                noteLengths.remove(currIndex);
                noteLengths.add(currIndex, temp);
                noteLengths.add(currIndex + 1, temp);
                again.add(currIndex);
//                splitCheck(new int[]{currIndex, currIndex+1}, Math.pow(chance, 5));
            }
//            else{
//                System.out.println("I MISSED a " + chance*100.0 + "% chance :(\n");
//            }
        }
        for (int id: again) {
            splitCheck(new int[]{id, id+1}, Math.pow(chance, 4.5));
        }

    }

    public void setNote(int index, String noteName){
        Note note = new Note(noteName, noteLengths.get(index));
        notes.set(index, note);
    }

    public int getNumNotes(){
        return numNotes;
    }

    public ArrayList<Note> getNotes(){
        return notes;
    }

}