package org.vors.pairbot.generator.standalone;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vors.pairbot.model.Person;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class StandalonePairGenerator {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public List<Pair<Person, Person>> generatePairs(List<Person> members) {
        List<Pair<Person, Person>> pairs = new ArrayList<>();

        List<Person> activeMembers = members.stream().filter(Person::isActive)
                .collect(Collectors.toCollection(LinkedList::new));

        List<Person> activeMasters = activeMembers.stream().filter(Person::isMaster)
                .collect(Collectors.toCollection(LinkedList::new));

        Person person;
        while (activeMasters.size() > 0) {
            person = Iterables.getFirst(activeMasters, null);
            Person other = findPair(person, activeMembers, activeMasters);

            pairs.add(new ImmutablePair<>(person, other));
        }
        return pairs;
    }

    public String projectHolderPrefix(Person person) {
        return person.isProjectHolder() ? "*" : "";
    }

    private Person findPair(Person currentUser, List<Person> members, List<Person> masters) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (members.size() > 1) {
            int pairIndex = random.nextInt(members.size());
            Person pair = members.get(pairIndex);
            if (pair != currentUser) {
                masters.remove(pair);
                members.remove(pairIndex);

                if(!masters.isEmpty()){
                    masters.remove(0);
                }
                if(!members.isEmpty()){
                    members.remove(0);
                }

                chooseProjectHolder(currentUser, pair);
                return pair;
            }
        }
        throw new IllegalStateException("Algorithm should never get there, check your logic");
    }

    private void chooseProjectHolder(Person currentUser, Person pair) {
        boolean firstIsProjectHolder = ThreadLocalRandom.current().nextBoolean();

        if(firstIsProjectHolder){
            currentUser.setProjectHolder(true);
        } else {
            pair.setProjectHolder(true);
        }
    }
}

