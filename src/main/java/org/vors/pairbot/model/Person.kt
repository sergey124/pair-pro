package org.vors.pairbot.model

class Person(var id: Int, var name: String?, var isMaster: Boolean) {
    var isActive = true
    var isProjectHolder: Boolean = false

    constructor(id: Int, name: String, master: Boolean, active: Boolean) : this(id, name, master) {
        this.isActive = active
    }
}
