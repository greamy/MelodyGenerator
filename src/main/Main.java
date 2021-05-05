package main;
import audio.player.AudioPlayer;
import generation.Generator;

import java.security.InvalidParameterException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean done = false;
        Generator gen = null;
        while(!done) {
            Scanner input = new Scanner(System.in);
            System.out.println("\n*All notes should be entered in the format [octave#][noteName][#/b], like 4C, or 5Eb*");
            System.out.println("Please enter the starting note of your range (The lower note, with no # or b): ");
//            String start = input.next();
            String start = "3C";

            System.out.println("Now please enter the ending note of your range (The higher note, with no #, or b): ");
//            String end = input.next();
            String end = "5C";

            System.out.println("Finally, please enter the key signature for the melody (All inputs assumed to be a major key):");
//            String keySig = input.next();
            String keySig = "Bb";

            try {
                gen = new Generator(start, end, keySig, 1, 0.8);
            }
            catch(InvalidParameterException e){
                System.out.println(e);
                continue;
            }
            done = true;
        }

        gen.generate();
        AudioPlayer player = new AudioPlayer(80, 100);
        player.run(gen.getNotes());
    }
}