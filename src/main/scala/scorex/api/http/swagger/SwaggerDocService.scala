package scorex.api.http.swagger

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka.model.{Info, License}
import com.github.swagger.akka.SwaggerHttpService
import com.wavesplatform.Version
import com.wavesplatform.settings.RestAPISettings
import io.swagger.models.{Swagger, Scheme}

class SwaggerDocService(val actorSystem: ActorSystem, val materializer: ActorMaterializer, val apiClasses: Set[Class[_]], settings: RestAPISettings)
  extends SwaggerHttpService {

  override val host: String = settings.bindAddress + ":" + settings.port
  override val info: Info = Info("The Web Interface to the VSYS Full Node API",
    Version.VersionString,
    "VSYS Full Node",
    "License: MIT License",
    None,
    Some(License("MIT License", "https://github.com/virtualeconomy/vsys/blob/master/LICENSE"))
  )

  //Let swagger-ui determine the host and port
  override val swaggerConfig: Swagger = new Swagger()
    .basePath(SwaggerHttpService.prependSlashIfNecessary(basePath))
    .info(info)
    .scheme(Scheme.HTTP)
    .scheme(Scheme.HTTPS)
}
