package generation;

public class Note {
    public String noteName;
    private final double duration;

    //String note: a note like "4C" or "3F"
    //double duration: a double where 2 is a half note, 1 = quarter note, 0.5 = eighth note, etc.
    public Note(String note, double duration){
        this.noteName = note;
        this.duration = duration;
    }

    // Gives milisecond length of note, for use in AudioPlayer.play() method
    public int toMiliseconds(int bpm){
        int miliPerBeat = 60000/bpm;
        return (int)(miliPerBeat * duration);
    }

    public String toString(){
        return "(" + noteName + " " + duration + ")";
    }
}
