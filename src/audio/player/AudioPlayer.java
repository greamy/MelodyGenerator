package audio.player;

import generation.Note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;

public class AudioPlayer {
    // Need 3 separate lists of notes for different ways of naming notes (important for implementing key signatures).
    private static List<String> notesSh = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static List<String> notesFl = Arrays.asList("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B");
    private static List<String> notesOth = Arrays.asList("B#", " ", " ",   " ",  "Fb", "E#", " ",  " ",  " ",  " ",   " ", "Cb");
    private static MidiChannel[] channels;
    private static int INSTRUMENT = 0; // 0 is a piano, 9 is percussion, other channels are for other instruments
    private int volume = 80; // between 0 et 127
    private int bpm = 60; // beats per minute, defaults to 60.

    public AudioPlayer(){ // Blank constructor if you want to use default volume and bpm.

    }

    // Volume is an int between 0-127, bpm is how fast the melody is. (beats per minute)
    public AudioPlayer(int volume, int bpm){
        this.bpm = bpm;
        this.volume = volume;
    }

    // This function plays the entire melody created by the Generator class.
    public void run(ArrayList<Note> notes){
        try{
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            channels = synth.getChannels();
            Thread.sleep(2000); // This sleep allows time for the synth to connect to computer audio channel.
            // Without sleep here, first note(s) rhythm is not accurate.

//            Instrument[] instruments = synth.getLoadedInstruments();
//            for(int i=0; i < instruments.length; i++){
//                System.out.println(instruments[i]);
//            }
//            channels[1].programChange(0, 24);
//            for(int i=0; i< channels.length; i++){
//                System.out.println(channels[i]);
//            }

            for(int i=0; i < notes.size(); i++){
                Note tempNote = notes.get(i);
                play(tempNote);
            }
            Thread.sleep(1000); // This allows for time for final note to ring out
            // For some reason, without sleep here, the final note gets cut short.
        }
        catch(InterruptedException e){ // Thread.sleep() throws an InterruptedException, and the midi and synth have their own exceptions.
            throw new RuntimeException(e);
        }
        catch(MidiUnavailableException e){
            throw new RuntimeException(e);
        }
    }

    // Note note = Note object, which is the note the play() function should play. Ideally called within a for loop of a list of notes.
    // returns nothing.
    private void play(Note note) throws InterruptedException{
        String noteName = note.noteName;
        System.out.println("noteName = " + noteName);
        if(noteName.equals("R")){ // Checks for rest, and sleeps if it is rest.
            System.out.println("REST!");
            Thread.sleep(note.toMiliseconds(bpm));
            return;
        }
        channels[INSTRUMENT].noteOn(id(note.noteName), volume); // Turns on audio for given note.
        Thread.sleep(note.toMiliseconds(bpm)); // Waits for specified time, given by note length and bpm.
        channels[INSTRUMENT].noteOff(id(note.noteName)); // Turns off audio for given note.
    }

    // String note = name of note to obtain integer id, which is needed to play using channel.noteOn()
    // returns integer id of note name.
    // This function also checks all possible names of notes, for example 'Bb' vs 'A#'.
    private static int id(String note)
    {
        int octave = Integer.parseInt(note.substring(0, 1));
        int index = notesSh.indexOf(note.substring(1));
        if(index == -1){
            index = notesFl.indexOf(note.substring(1));
        }
        if(index == -1){
            index = notesOth.indexOf(note.substring(1));
        }
        return index + 12 * octave + 12;
    }
}