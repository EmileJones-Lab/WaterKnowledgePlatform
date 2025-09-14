package top.emilejones.huu.env

import net.mamoe.yamlkt.Yaml
import top.emilejones.huu.env.pojo.ApplicationConfig
import java.io.File
import java.io.IOException

object AutoFindConfigFile: ConfigFileReader {
    private var config: ApplicationConfig? = null

    override fun find(): ApplicationConfig {
        if (config != null)
            return config as ApplicationConfig

        val yamlFile = File("./config.yml")
        if (!yamlFile.exists())
            throw IOException("Can't find the config file, please create [config.yml]")
        val yamlContent = yamlFile.readText()
        return Yaml.decodeFromString(ApplicationConfig.serializer(), yamlContent)
    }
}