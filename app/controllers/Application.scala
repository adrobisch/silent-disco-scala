package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator}
import play.api.libs.json.{JsValue, JsObject}
import model.Room

object Application extends Controller {
  
  def joinRoom (username: String) = WebSocket.async[JsValue] { request  =>
    Room.join(username)
  }

  def welcome = Action {
    Ok(views.html.index("Welcome to Silent Disco"))
  }

  def index = Action {
    Redirect("/sd/app/index.html")
  }
}