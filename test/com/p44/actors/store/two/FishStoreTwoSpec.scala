package com.p44.actors.store.two

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import com.p44.models._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._

object FishStoreTwoSpec extends Specification {

  //sbt > test-only com.p44.actors.store.two.FishStoreTwoSpec

  sequential

  val duration = new FiniteDuration(5, SECONDS)
  implicit val timeout = Timeout (duration)
  
  val delivery = List(Fish("trout", 2.5), Fish("trout", 2.6), Fish("mackerel", 3.5))

  "FishStoreTwo" should {
    "Deliver" in {
        implicit val system = ActorSystem("TestSys")
        val controller = system.actorOf(FishStoreTwo.propsController)
        val p = TestProbe()
        p.send(controller, FishStoreTwo.Echo)
        val e1: String = p.expectMsg("Echo")
        e1 mustEqual "Echo"
        
        val w = p.watch(controller)
        println("Deliver w " + w) 
        
        val f = (controller ask FishStoreTwo.Deliver(delivery)).mapTo[DeliveryReceipt]
        val rDr: DeliveryReceipt = Await.result(f, duration)
        println("Deliver rDr " + rDr) // DeliveryReceipt(1390593161821,3,8.6,25.799999999999997,01/24/2014 12:52:41,Thank You!)
        system.shutdown
        system.awaitTermination
        rDr.fishCount mustEqual 3
        rDr.totalWeight mustEqual 8.6
        rDr.message mustEqual "Thank You!"
        rDr.payment > 25 mustEqual true
    }
  }

}
