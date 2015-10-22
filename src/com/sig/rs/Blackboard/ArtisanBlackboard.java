package com.sig.rs.Blackboard;
import org.powerbot.script.rt6.Component;

public class ArtisanBlackboard extends Blackboard {
    public static final int[][] levels = {
            //rails, plate, 40%, spikes, 60%, joint, 80%, tie, 100%
            { 1,     2,     3,   5,      6,   8,     9,   11,  12},
            {15,     19,    20,  24,     25,  29,    30,  34,  35},
            {39,     44,    45,  49,     50,  54,    55,  59,  60},
    };
    public static final int[] ingots = {
            20502,
            20503,
            20504,
    };
    public static final int[][] components = {
            //rails, plate, 40%,   spikes, 60%,   joint, 80%,   tie,   100%
            {20506,  20507, 20511, 20508,  20512, 20509, 20513, 20510, 20514},
            {20515,  20516, 20525, 20517,  20526, 20518, 20527, 20519, 20528},
            {20520,  20521, 20529, 20522,  20530, 20523, 20531, 20524, 20532},
    };
    public static final int[] troughIds = {24821, 24822, 24823}; //Take-ingots

    public static final int anvilId = 24820; //Smith
    public static final int mineCartId = 24824; //Deposit-components
    public static final int[] tunnelIds = {24826, 24743, 24825}; //Lay-tracks

    public static final String[] ingotNames = {"Bronze", "Iron", "Steel"};
    public static final String[] componentNames = {"Rails", "Base Plate", "Track 40%", "Spikes", "Track 60%", "Joint", "Track 80%", "Ties", "Track 100%"};

    public static BlackboardProperty<Integer> smithingLevel = new BlackboardProperty<Integer>(0);
    public static BlackboardProperty<Integer> smithingRealLevel = new BlackboardProperty<Integer>(0);
    public static BlackboardProperty<Integer> smithingLevelRealStart = new BlackboardProperty<Integer>(0);
    public static BlackboardProperty<Integer> smithingExp = new BlackboardProperty<Integer>(0);
    public static BlackboardProperty<Integer> smithingExpStart = new BlackboardProperty<Integer>(0);
    public static BlackboardProperty<Integer> ingotToUse = new BlackboardProperty<Integer>(0);
    public static BlackboardProperty<Integer> highestComponentToSmith = new BlackboardProperty<Integer>(0);
    public static BlackboardProperty<Integer> currentComponentToSmith = new BlackboardProperty<Integer>(0);

    public static BlackboardProperty<Component> smithComponentsWrapperComponent = new BlackboardProperty<Component>();
    public static BlackboardProperty<Component> smithTakeButton = new BlackboardProperty<Component>();
    public static BlackboardProperty<Component> smithingInProggressComponent = new BlackboardProperty<Component>();

    public static String getIngotToUseName() {
        return ingotNames[ingotToUse.get()];
    }
    public static String getHighestComponentToSmithName() {
        return componentNames[highestComponentToSmith.get()];
    }
    public static String getCurrentComponentToSmithName() {
        return componentNames[currentComponentToSmith.get()];
    }
}
