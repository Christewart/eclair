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

import java.net.{InetAddress, InetSocketAddress}

import fr.acinq.eclair.db.sqlite.SqlitePeersDb
import fr.acinq.eclair.{TestConstants, randomKey}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

@RunWith(classOf[JUnitRunner])
class SqlitePeersDbSpec extends FunSuite with BeforeAndAfterAll {

  private val dbConfig = ??? //TestConstants.dbConfig
  private val db = new SqlitePeersDb(dbConfig)

  override def beforeAll(): Unit = {
    db.createTables
  }

  test("init sqlite 2 times in a row") {
    val db1 = new SqlitePeersDb(dbConfig)
    val db2 = new SqlitePeersDb(dbConfig)
    db1.createTables
    db2.createTables
  }

  test("add/remove/list peers") {
    val db = new SqlitePeersDb(dbConfig)

    val peer_1 = (randomKey.publicKey, new InetSocketAddress(InetAddress.getLoopbackAddress, 1111))
    val peer_1_bis = (peer_1._1, new InetSocketAddress(InetAddress.getLoopbackAddress, 1112))
    val peer_2 = (randomKey.publicKey, new InetSocketAddress(InetAddress.getLoopbackAddress, 2222))
    val peer_3 = (randomKey.publicKey, new InetSocketAddress(InetAddress.getLoopbackAddress, 3333))

    assert(db.listPeers().toSet === Set.empty)
    db.addOrUpdatePeer(peer_1._1, peer_1._2)
    db.addOrUpdatePeer(peer_1._1, peer_1._2) // duplicate is ignored
    assert(db.listPeers().size === 1)
    db.addOrUpdatePeer(peer_2._1, peer_2._2)
    db.addOrUpdatePeer(peer_3._1, peer_3._2)
    assert(db.listPeers().toSet === Set(peer_1, peer_2, peer_3))
    db.removePeer(peer_2._1)
    assert(db.listPeers().toSet === Set(peer_1, peer_3))
    db.addOrUpdatePeer(peer_1_bis._1, peer_1_bis._2)
    assert(db.listPeers().toSet === Set(peer_1_bis, peer_3))
  }

  override def afterAll: Unit = {
    db.dropTables
    //dbConfig.close()
  }

}
