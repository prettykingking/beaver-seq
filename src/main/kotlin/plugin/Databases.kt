package org.jiezheng.plugin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val dbConfig = environment.config.config("storage")
    val hikariConfig = HikariConfig().apply {
        driverClassName = dbConfig.property("driverClass").getString()
        jdbcUrl =  dbConfig.property("url").getString()
        username =  dbConfig.property("user").getString()
        password =  dbConfig.property("password").getString()
        maximumPoolSize =  dbConfig.property("poolSize").getString().toInt()
        validate()
    }

    Database.connect(HikariDataSource(hikariConfig))
}
