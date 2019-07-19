package org.vors.pairbot.generator;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vors.pairbot.model.Person;

import java.util.*;

public class PairGeneratorTest {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private PairGenerator systemUnderTest = new PairGenerator();

    List<Person> members;

    @Before
    public void setup() {
        members = new ArrayList<>();

        members.add(new Person(0, "A", true));
        members.add(new Person(1, "B", true));
        members.add(new Person(2, "C", true));
        members.add(new Person(3, "D", true));
        members.add(new Person(4, "E", true));
        members.add(new Person(5, "F", true));
    }

    @Test
    public void testGeneratePairs() {
        List<Pair<Person, Person>> pairs = systemUnderTest.generatePairs(members);

        boolean membersNotRepete = membersNotRepete(pairs);
        if(!membersNotRepete){
            LOG.warn("Members repete in pairs!");
        }

        Assert.assertTrue(membersNotRepete);

        boolean allMastersHavePairs = allMastersHavePairs(members, pairs);
        if(!allMastersHavePairs){
            LOG.warn("Not all members have pairs!");
        }
        Assert.assertTrue(allMastersHavePairs);

        //todo: test all masters have pairs

        pairs.forEach(pair -> {
            Person left = pair.getLeft();
            String firstMemberLabel = systemUnderTest.projectHolderPrefix(left) + left.getName();
            Person right = pair.getRight();
            String secondMemberLabel = systemUnderTest.projectHolderPrefix(right) + right.getName();
            LOG.info(String.format("%s, %s", firstMemberLabel, secondMemberLabel));
        });
    }

    private boolean allMastersHavePairs(List<Person> members, List<Pair<Person, Person>> pairs) {
        Set<Person> pairedMembers = new HashSet<>();
        pairs.forEach(pair -> {
            pairedMembers.add(pair.getRight());
            pairedMembers.add(pair.getLeft());
        });
        return members.stream().filter(Person::isActive).filter(Person::isMaster).allMatch(pairedMembers::contains);
    }

    private boolean membersNotRepete(List<Pair<Person, Person>> pairs) {
        Set<Person> persons = new HashSet<>(pairs.size() * 2);

        pairs.forEach(pair -> {
            persons.add(pair.getLeft());
            persons.add(pair.getRight());
        });
        return persons.size() == pairs.size() * 2;
    }


}


