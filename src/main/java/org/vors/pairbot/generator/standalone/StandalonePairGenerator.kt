package org.vors.pairbot.generator.standalone

import com.google.common.collect.Iterables
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.vors.pairbot.model.Person

import java.util.ArrayList
import java.util.LinkedList
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

class StandalonePairGenerator {
    private val LOG = LoggerFactory.getLogger(javaClass)

    fun generatePairs(members: List<Person>): List<Pair<Person, Person>> {
        val pairs = ArrayList<Pair<Person, Person>>()

        val activeMembers = members.stream().filter(Predicate<Person> { it.isActive() })
                .collect<LinkedList<Person>, Any>(Collectors.toCollection(Supplier<LinkedList<Person>> { LinkedList() }))

        val activeMasters = activeMembers.stream().filter(Predicate<Person> { it.isMaster() })
                .collect<LinkedList<Person>, Any>(Collectors.toCollection(Supplier<LinkedList<Person>> { LinkedList() }))

        var person: Person?
        while (activeMasters.size > 0) {
            person = Iterables.getFirst(activeMasters, null)
            val other = findPair(person, activeMembers, activeMasters)

            pairs.add(ImmutablePair(person, other))
        }
        return pairs
    }

    fun projectHolderPrefix(person: Person): String {
        return if (person.isProjectHolder) "*" else ""
    }

    private fun findPair(currentUser: Person?, members: MutableList<Person>, masters: MutableList<Person>): Person {
        val random = ThreadLocalRandom.current()
        while (members.size > 1) {
            val pairIndex = random.nextInt(members.size)
            val pair = members[pairIndex]
            if (pair !== currentUser) {
                masters.remove(pair)
                members.removeAt(pairIndex)

                if (!masters.isEmpty()) {
                    masters.removeAt(0)
                }
                if (!members.isEmpty()) {
                    members.removeAt(0)
                }

                chooseProjectHolder(currentUser, pair)
                return pair
            }
        }
        throw IllegalStateException("Algorithm should never get there, check your logic")
    }

    private fun chooseProjectHolder(currentUser: Person?, pair: Person) {
        val firstIsProjectHolder = ThreadLocalRandom.current().nextBoolean()

        if (firstIsProjectHolder) {
            currentUser!!.isProjectHolder = true
        } else {
            pair.isProjectHolder = true
        }
    }
}

