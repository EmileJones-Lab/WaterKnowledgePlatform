package top.emilejones.huu.env

import top.emilejones.huu.env.pojo.ApplicationConfig

interface ConfigFileReader {
    fun find(): ApplicationConfig
}