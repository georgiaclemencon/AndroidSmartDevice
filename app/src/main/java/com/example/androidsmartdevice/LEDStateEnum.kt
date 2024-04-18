package com.example.androidsmartdevice

enum class LEDStateEnum(val value: ByteArray) {
    LED_1(byteArrayOf(0x01)),
    LED_2(byteArrayOf(0x02)),
    LED_3(byteArrayOf(0x05)),
    NONE(byteArrayOf(0x00))
}