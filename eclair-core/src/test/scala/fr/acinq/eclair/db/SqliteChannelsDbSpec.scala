/*
 * Copyright 2018 ACINQ SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.acinq.eclair.db

import java.sql.DriverManager

import fr.acinq.bitcoin.BinaryData
import fr.acinq.eclair.TestConstants
import fr.acinq.eclair.db.sqlite.{SqliteChannelsDb, SqlitePendingRelayDb, SqliteUtils}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.sqlite.SQLiteException

@RunWith(classOf[JUnitRunner])
class SqliteChannelsDbSpec extends FunSuite with BeforeAndAfterAll {

  def inmem = DriverManager.getConnection("jdbc:sqlite::memory:")
  private val dbConfig = TestConstants.dbConfig
  test("init sqlite 2 times in a row") {
    val sqlite = inmem
    val db1 = new SqliteChannelsDb(dbConfig)
    val db2 = new SqliteChannelsDb(dbConfig)
  }

  test("add/remove/list channels") {

    val db = new SqliteChannelsDb(dbConfig)
    db.createTables
    val res: Int = SqliteUtils.using(db.dbConfig.getConnection().createStatement()) { statement =>
      statement.executeQuery(s"SELECT count(*) FROM ${db.DB_NAME} WHERE type='table' AND name='local_channels'").getInt(0)
    }
    assert(res == 1)
    new SqlitePendingRelayDb(dbConfig) // needed by db.removeChannel

    val channel = ChannelStateSpec.normal

    val commitNumber = 42
    val paymentHash1 = BinaryData(".s42" * 300)
    val cltvExpiry1 = 123
    val paymentHash2 = BinaryData("43" * 300)
    val cltvExpiry2 = 656

    intercept[SQLiteException](db.addOrUpdateHtlcInfo(channel.channelId, commitNumber,
      paymentHash1, cltvExpiry1)) // no related channel

    assert(db.listChannels().toSet === Set.empty)
    db.addOrUpdateChannel(channel)
    db.addOrUpdateChannel(channel)
    assert(db.listChannels() === List(channel))

    assert(db.listHtlcHtlcInfos(channel.channelId, commitNumber).toList == Nil)
    db.addOrUpdateHtlcInfo(channel.channelId, commitNumber, paymentHash1, cltvExpiry1)
    db.addOrUpdateHtlcInfo(channel.channelId, commitNumber, paymentHash2, cltvExpiry2)
    assert(db.listHtlcHtlcInfos(channel.channelId, commitNumber).toList == List((paymentHash1, cltvExpiry1), (paymentHash2, cltvExpiry2)))
    assert(db.listHtlcHtlcInfos(channel.channelId, 43).toList == Nil)

    db.removeChannel(channel.channelId)
    assert(db.listChannels() === Nil)
    assert(db.listHtlcHtlcInfos(channel.channelId, commitNumber).toList == Nil)
  }

}
