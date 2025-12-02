package top.emilejones.hhu.env

import top.emilejones.hhu.env.pojo.ApplicationConfig

interface ConfigFileReader {
    fun find(): ApplicationConfig
}