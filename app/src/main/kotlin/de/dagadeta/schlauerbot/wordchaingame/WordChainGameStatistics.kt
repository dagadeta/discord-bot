package de.dagadeta.schlauerbot.wordchaingame

import de.dagadeta.schlauerbot.persistance.UsedWordRepository
import org.springframework.stereotype.Service

@Service
class WordChainGameStatistics(private val usedWordRepo: UsedWordRepository) {

    fun getTotalWords() = usedWordRepo.count()

    fun getMostCommonEndingLetters(limit: Int = 10): Map<Char, Int> {
        return usedWordRepo.findAll()
            .groupingBy { it.word.last().lowercaseChar() }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .toMap()
    }

    fun getLongestWord() = usedWordRepo.findAll()
        .maxByOrNull { it.word.length }?.word

    fun getAverageWordLength(): Double {
        val words = usedWordRepo.findAll()
        return if (words.isEmpty()) 0.0
        else words.sumOf { it.word.length }.toDouble() / words.size
    }

}
