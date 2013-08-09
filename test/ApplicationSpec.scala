import org.slf4j.LoggerFactory
import org.specs2.mutable._
import org.specs2.matcher.Matcher

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  val logger = LoggerFactory.getLogger(classOf[ApplicationSpec])

  class KarmaRun(configFile: String) {
    val confPath = getClass().getClassLoader.getResource(configFile).getFile

    def run = {
      val builder = new ProcessBuilder("karma", "start", confPath)
      val karmaProcess = builder.start()

      println("karma runner starting")
      println(scala.io.Source.fromInputStream(karmaProcess.getInputStream()).getLines().mkString("\n"))

      karmaProcess.waitFor()
    }

    override def toString = {
      configFile
    }
  }

  val runSuccessFully: Matcher[KarmaRun] = ((_: KarmaRun).run == 0, "karma run succesful", "karma run has errors")

  "Application" should {
    
    "redirect to the client page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/")).get
        redirectLocation(home) must equalTo(Some("/sd/app/index.html"))
      }
    }

    "implement the silent disco client protocol" in {
      running(TestServer(3333, FakeApplication())) {
        new KarmaRun("karma.conf.js") must runSuccessFully
      }
    }
  }

}