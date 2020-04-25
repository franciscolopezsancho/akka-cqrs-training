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


class BoxSpec extends ScalaTestWithActorTestKit(ConfigFactory.load()) with AnyWordSpecLike {

  def randomId = "box"+scala.util.Random.nextInt(Int.MaxValue)

  "The box object" should {
    "accept Commands, transform them in events and persist" in {
      val maxCapacity = 10
      val cart = testKit.spawn(Box(randomId,maxCapacity))
      val probe = testKit.createTestProbe[Confirmation]()
      cart ! AddItem("bar",1,probe.ref)
      probe.expectMessage(Accepted(9))
    }
  }

  "The box object" should {
    "Reject after second item surpasses the max capacity" in {
     val maxCapacity = 10
      val cart = testKit.spawn(Box(randomId,maxCapacity))
      val probe = testKit.createTestProbe[Confirmation]()
      cart ! AddItem("bar",1,probe.ref)
      probe.expectMessage(Accepted(9))

      cart ! AddItem("bar2",1,probe.ref)
      probe.expectMessage(Accepted(8))

      cart ! AddItem("foo",11,probe.ref)
      probe.expectMessage(Rejected(Item("foo",11),8))
    }
  }

 
    
  

  
}
