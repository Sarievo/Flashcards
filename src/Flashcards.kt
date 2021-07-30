import java.io.File

val cards = mutableMapOf<String, String>()
var runtimeLog = ""
val errorLog = mutableMapOf<String, Int>()

var timer = 1
var isSavedSession = false
var savedFile = ""

fun logRead(): String {
    val str = java.util.Scanner(System.`in`).nextLine()!!.trim()
    runtimeLog += "[$timer] I: $str\n"; timer++
    return str
}

fun logPrint(str: String) {
    runtimeLog += "[$timer] System: $str\n"; timer++
    println(str)
}

fun main(args: Array<String>) {
    for (i in args.indices - 1) {
        if (args[i] == "-import") import(args[i + 1])
        else if (args[i] == "-export") {
            isSavedSession = true
            savedFile = args[i + 1]
        } else continue
    }

    play()
}

fun play() {
    while (true) {
        logPrint("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        when (logRead()) {
            "add" -> add()
            "remove" -> remove()
            "import" -> {
                logPrint("File name:")
                import(logRead())
            }
            "export" -> {
                logPrint("File name:")
                export(logRead())
            }
            "ask" -> ask()
            "exit" -> {
                println("Bye Bye!")
                if (isSavedSession) export(savedFile)
                break
            }
            "log" -> log()
            "hardest card" -> hardestCard()
            "reset stats" -> resetStats()
        }
        println()
    }
    /*
    println("Input the number of cards:")
    val cardNums = readLine()!!.toInt()

    for (i in 1 .. cardNums) {
        println("Card #$i")
        var term = readLine()!!
        while (term in cards.keys) {
            println("The term \"$term\" already exists. Try again:")
            term = readLine()!!
        }

        println("The definition for card #$i")
        var definition = readLine()!!
        while (definition in cards.values) {
            println("The definition \"$definition\" already exists. Try again:")
            definition = readLine()!!
        }
        cards[term] = definition
    }

    for (card in cards) {
        println("Print the definition of \"${card.key}\":")
        when (val answer = readLine()!!) {
            card.value -> println("Correct!")
            in cards.values -> println("Wrong. The right answer is \"${card.value}\", " +
                    "but your definition is correct for " +
                    "\"${cards.filter { it.value == answer }.keys.joinToString()}\"."
            )
            else -> println("Wrong. The right answer is \"${card.value}\".")
        }
    }
    */
}

fun add() {
    logPrint("The card:")
    val card = logRead()
    if (card in cards.keys) {
        logPrint("The card \"$card\" already exists."); return
    }

    logPrint("The definition of the card:")
    val definition = logRead()
    if (definition in cards.values) {
        logPrint("The definition \"$definition\" already exists."); return
    }

    cards[card] = definition
    logPrint("The pair (\"$card\":\"$definition\") has been added.")
}

fun remove() {
    logPrint("Which card?")
    val card = logRead()
    if (card in cards.keys) {
        cards.remove(card)
        logPrint("The card has been removed.")
    } else {
        logPrint("Can't remove \"$card\": there is no such card.")
    }
}

fun import(str: String) {
    if (!File(str).exists()) {
        logPrint("File not found.")
    } else {
        val lines = File(str).readLines()
        for (line in lines) {
            val (key, value, times) = line.split(":")
            cards[key] = value
            if (times.toInt() > 0) errorLog[key] = times.toInt()
        }
        logPrint("${lines.size} cards have been loaded.")
    }
}

fun export(str: String) {
    var text = ""
    for ((key, value) in cards) {
        text += if (key in errorLog) {
            "$key:$value:${errorLog[key]}\n"
        } else "$key:$value:0\n"
    }

    File(str).writeText(text)
    logPrint("${cards.size} cards have been saved.")
}

fun ask() {
    if (cards.isEmpty()) { println("There is no card in your deck, try add some."); return }

    logPrint("How many times to ask?")
    var times: Int
    try { times = logRead().toInt() } catch (e: Exception) { return }

    while (times > 0) for ((key, value) in cards) {
        if (times > 0) {
            logPrint("Print the definition of \"$key\":")
            when (val answer = logRead()) {
                value -> logPrint("Correct!")
                in cards.values -> {
                    logPrint("Wrong. The right answer is \"$value\", "
                            + "but your definition is correct for "
                            + "\"${cards.filter { it.value == answer }.keys.joinToString()}\"."
                    )
                    logError(key)
                }
                else -> {
                    logPrint("Wrong. The right answer is \"$value\".")
                    logError(key)
                }
            }
            times--
        }
    }
}

fun logError(key: String) {
    if (key in errorLog.keys) {
        errorLog[key]?.plus(1)?.let { errorLog.replace(key, it) }
    } else {
        errorLog[key] = 1
    }
}

fun log() {
    logPrint("File name:")
    val file = logRead()

    File(file).writeText(runtimeLog)
    logPrint("The log has been saved.")
}

fun hardestCard() {
    if (errorLog.isNotEmpty()) {
        val text: String
        var mostErr = 0
        for (value in errorLog.values) {
            if (value > mostErr) mostErr = value
        }

        val listErr = mutableListOf<String>()
        for ((key, value) in errorLog) {
            if (value == mostErr) listErr.add(key)
        }

        text = "The hardest card" + if (listErr.size > 1) {
            "s are \"${listErr.joinToString("\", \"")}\". \"You have $mostErr errors answering them.\""
        } else {
            " is \"${listErr[0]}\". \"You have $mostErr errors answering it.\""
        }
        logPrint(text)
    } else logPrint("There are no cards with errors.")
}

fun resetStats() {
    errorLog.clear()
    logPrint("Card statistics have been reset.")
}
