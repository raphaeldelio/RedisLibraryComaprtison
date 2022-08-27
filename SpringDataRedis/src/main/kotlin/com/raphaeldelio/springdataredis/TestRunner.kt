package com.raphaeldelio.springdataredis

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch

@Component
class TestRunner(
    private val airportRepository: AirportRepository,
    private val redisTemplate: RedisTemplate<String, Any>
    ) : CommandLineRunner {
    override fun run(vararg args: String) {
        val spentTimeHash = mutableListOf<Long>()

        for (x in 0..NUMBER_OF_RUNS) {
            flushAll()
            spentTimeHash.add(testHash())
        }

        val avgTimeSpentHash = spentTimeHash.average()
        val avgPerInsertHash = avgTimeSpentHash / NUMBER_OF_KEYS

        logger.info("Hash: It took an avg of $avgTimeSpentHash ms to insert $NUMBER_OF_KEYS in the database")
        logger.info("Hash: It took an avg of $avgPerInsertHash ms to insert each key in the database")
    }

    private fun testHash(): Long {
        val airport = Airport(
            id = "5910",
            ident = "SBGR",
            name = "Guarulhos - Governador André Franco Montoro International Airport",
            type = "airport",
            latitudeDeg = -23.431944,
            longitudeDeg = -46.467778,
            elevationFt = 2461,
            continent = "SA",
            isoCountry = "BR",
            isoRegion = "BR-SP",
            municipality = "São Paulo",
            scheduledService = "yes",
            gpsCode = "SBGR",
            iataCode = "GRU",
            localCode = "SP0002",
            homeLink = "http://www.aeroportoguarulhos.net/",
            wikipediaLink = "https://en.wikipedia.org/wiki/S%C3%A3o_Paulo-Guarulhos_International_Airport",
            keywords = "Cumbica"
        )

        val airportList = mutableListOf<Airport>()
        for (x in 1..NUMBER_OF_KEYS) {
            airportList.add(
                airport.copy(id = x.toString())
            )
        }

        logger.info("HashMap: Inserting airports")
        val stopWatch = StopWatch()
        stopWatch.start()
        airportRepository.saveAll(airportList)
        stopWatch.stop()

        logger.info("HashMap: Insert Total time: ${stopWatch.totalTimeMillis} ms")
        return stopWatch.totalTimeMillis
    }

    private fun flushAll() {
        redisTemplate.execute {
            logger.info("Flushing ${it.dbSize()} keys.")
            it.flushAll()
            logger.info("${it.dbSize()} keys in the db")
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
        private const val NUMBER_OF_KEYS = 1000
        private const val NUMBER_OF_RUNS = 10
    }
}