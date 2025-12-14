package top.emilejones.hhu.common.env

import top.emilejones.hhu.common.env.pojo.ApplicationConfig

interface ConfigFileReader {
    fun find(): ApplicationConfig
}