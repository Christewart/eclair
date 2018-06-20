package fr.acinq.eclair.db

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import fr.acinq.eclair.db.sqlite.SqliteUtils.getVersion
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DbConfigTest extends FunSuite {

  test("read regtest database configuration") {
    val config = ConfigFactory.load()
    val dbConfig = DbConfig.unittestConfig(config)
    val conn = dbConfig.getConnection()
    val statement = conn.createStatement()
    val result = getVersion(statement, "channels", 1)
    assert(result == 1)
    assert(!conn.isClosed)
    conn.close()
    assert(conn.isClosed)
  }
}
