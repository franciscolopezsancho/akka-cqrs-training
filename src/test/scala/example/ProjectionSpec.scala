package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import Box._
import com.typesafe.config.ConfigFactory
import akka.persistence.query.PersistenceQuery
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.stream.scaladsl.Source
import akka.persistence.query.EventEnvelope
import akka.NotUsed
import akka.stream.alpakka.slick.scaladsl.Slick
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api._
import org.scalatest.concurrent.PatienceConfiguration
import akka.stream.scaladsl.Sink
import scala.concurrent.Await
import scala.concurrent.duration._

class ProjectionSpec
    extends ScalaTestWithActorTestKit(ConfigFactory.load())
    with AnyWordSpecLike {

  def randomId = "box"+scala.util.Random.nextInt(Int.MaxValue)

  "A projection" should {
    "be able to write to produce a new table" in {

      val boxId = randomId

      val maxCapacity = 10
      val cart = testKit.spawn(Box(boxId, maxCapacity))
      val probe = testKit.createTestProbe[Confirmation]()
      cart ! AddItem("bar", 1, probe.ref)
      probe.expectMessage(Accepted(9))

      val query = PersistenceQuery(system)
        .readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

      val db = DatabaseConfig.forConfig[JdbcProfile](
        "akka-persistence-jdbc.shared-databases.slick"
      )
      implicit val slickSession: SlickSession = SlickSession.forConfig(db)

      val source: Source[String, NotUsed] =
        query.persistenceIds()

      source.runWith(
        // add an optional first argument to specify the parallelism factor (Int)
        Slick.sink(id => insertEvent(id))
      )

      eventually(PatienceConfiguration.Timeout(10.seconds)) {
        val future = Slick
          .source(sql"select * from my_projection".as[String])
          .runWith(Sink.seq)
        val result = Await.result(future, 1.second)
        println(s"#################### $result")
        result should contain(s"Box|$boxId")
      }
    }

    def insertEvent(event: String): DBIO[Int] =
      sqlu"INSERT INTO my_projection VALUES($event)"

  }

}
