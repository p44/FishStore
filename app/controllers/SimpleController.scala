package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.mvc.Action
import play.api.mvc.Controller

/**
 * Index page, hello page and other simple returns
 */
object SimpleController extends Controller { // Defines utility methods to generate Action and Results types.

  lazy val homeMsg: String = "This is Fish Store"
  lazy val helloMsg: String = "Hello There" 

  def index = Action { // request => response
	Ok(homeMsg) // 200
  }
  
  def hello(name: String) = Action.async { // request => future response
	Future {
	  Ok(helloMsg + ", " + name + ". " + homeMsg)
	}
  }
  
  // Play allows the return of a Future Result

}
