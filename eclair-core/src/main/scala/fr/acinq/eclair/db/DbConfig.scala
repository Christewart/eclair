package fr.acinq.eclair.db

import java.io.File
import java.sql.Connection

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import grizzled.slf4j.Logging

class DbConfig(hikariConfig: HikariConfig) extends Logging {

  private val dataSource: HikariDataSource = {
    val jdbcUrl = hikariConfig.getJdbcUrl
    if (jdbcUrl.contains("sqlite")) {

      val chaindir = new File(jdbcUrl.split(":").last)
      val eclairSqlite = new File(chaindir, "eclair.sqlite")
      val networkSqlite = new File(chaindir, "network.sqlite")
      if (chaindir.mkdirs()) {
        logger.info(s"Creating new file for chaindir ${chaindir.getAbsolutePath}")
      }

      if (eclairSqlite.createNewFile()) {
        logger.info(s"Creating new eclair database ${eclairSqlite.getAbsolutePath}")
      }

      if (networkSqlite.createNewFile()) {
        logger.info(s"Creating new network database ${networkSqlite.getAbsolutePath}")
      }
    }
    new HikariDataSource(hikariConfig)
  }

  def getConnection(): Connection = dataSource.getConnection

  /** Closes [[com.zaxxer.hikari.HikariDataSource]] */
  def close(): Unit = dataSource.close()

  def isClosed(): Boolean = dataSource.isClosed

  def isRunning(): Boolean = dataSource.isRunning
}


object DbConfig {


  /** Reads the network you want from the reference.conf file */
  def fromConfig(config: Config): DbConfig = {
    val chain = config.getString("eclair.chain")
    fromConfig(config,chain)
  }

  private def fromConfig(config: Config, chain: String): DbConfig = {
    val dbUrl = config.getString(s"eclair.db.${chain}.url")
    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(dbUrl)
    new DbConfig(hikariConfig)
  }

  def mainnetConfig(config: Config): DbConfig = {
    fromConfig(config, "mainnet")
  }

  def testnetConfig(config: Config): DbConfig = {
    fromConfig(config,"testnet")
  }

  def regtestConfig(config: Config): DbConfig = {
    fromConfig(config,"regtest")
  }

  def unittestConfig(config: Config): DbConfig = {
    fromConfig(config,"unittest")
  }
}