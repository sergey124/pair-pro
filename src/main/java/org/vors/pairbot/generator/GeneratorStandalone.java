package org.vors.pairbot.generator;

import org.apache.commons.lang3.tuple.Pair;
import org.vors.pairbot.model.Person;

import java.util.ArrayList;
import java.util.List;

public class GeneratorStandalone {
    private static PairGenerator pairGenerator = new PairGenerator();
    private static List<Person> members;

    static {
        members = new ArrayList<>();

        members.add(new Person(0, "Sergey", true));
        members.add(new Person(1, "Vlad", true));
        members.add(new Person(2, "Maksim", true));
        members.add(new Person(3, "Elena", true));
        members.add(new Person(4, "Nikita", true));
    }

    public static void main(String[] args){
        List<Pair<Person, Person>> pairs = pairGenerator.generatePairs(members);

        System.out.println("Pairs for this week:");
        pairs.forEach(pair -> {
            Person left = pair.getLeft();
            String firstMemberLabel = pairGenerator.projectHolderPrefix(left) + left.getName();
            Person right = pair.getRight();
            String secondMemberLabel = pairGenerator.projectHolderPrefix(right) + right.getName();
            System.out.println(String.format("%s, %s", firstMemberLabel, secondMemberLabel));
        });
    }
}
