package controllers

import play.api.mvc.{Action, Controller}
import play.api.Play.current
import play.api.Play

/**
 *
 * @author drobisch
 */
object Config extends Config

class Config extends Controller {
  def soundCloudClient =  Action {
    Ok(views.html.client(Play.configuration.getString("soundcloud.client.id").getOrElse("")))
  }
}
